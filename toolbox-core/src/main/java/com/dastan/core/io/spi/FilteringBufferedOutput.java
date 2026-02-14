package com.dastan.core.io.spi;

import com.dastan.core.io.buffer.BufferedOutput;
import com.dastan.core.io.buffer.NestedBufferedOutput;
import com.dastan.core.io.buffer.TopLevelBufferedOutput;

import java.io.EOFException;
import java.io.IOException;

/// Base class for filtering [BufferedOutput] implementations that process the output data and
/// append it to a parent buffer.
/// 
/// Synchronous filters need to implement at least [#filterBlock(BufferedOutput)] and potentially
/// override [#finish()]. Asynchronous filters must also override [#flushPending()] (but most
/// asynchronous filters will want to extend [AsyncFilteringBufferedOutput] instead).
public abstract class FilteringBufferedOutput extends TopLevelBufferedOutput {
  protected final BufferedOutput parent;
  protected final boolean flushPartial;

  /// An exclusive block from the cache whose buffer is used by this block. We swap it back into
  /// that block to either return it to the cache or insert it into the parent at the end (both of
  /// which require a standard NestedBufferedOutput block).
  private NestedBufferedOutput cachedBlock;

  /// Constructor to be called by subclasses.
  ///
  /// @param parent The parent buffer to write to.
  /// @param flushPartial When set to `true`, the first open block can be partially flushed. This
  ///   is generally desirable for flushing, but the filter implementation must support it, and
  ///   it can be less efficient than only flushing closed blocks.
  protected FilteringBufferedOutput(BufferedOutput parent, boolean flushPartial) {
    super(null, parent.isBigEnd(), 0, 0, 0, parent.cache.getInitialBufferSize(), false, Long.MAX_VALUE, parent.cache, !flushPartial);
    cachedBlock = cache.getExclusiveBlock();
    buffer = cachedBlock.getBuffer();
    limit = buffer.length;
    cachedBlock.setBuffer(null);
    this.parent = parent;
    this.flushPartial = flushPartial;
  }

  @Override
  protected void bufferClosed(boolean closeUpstream) throws IOException {
    super.bufferClosed(closeUpstream);
    finish();
    cleanUp();
    if(closeUpstream) parent.close();
  }

  /// Close the filter without closing or flushing the parent.
  protected void finish() throws IOException {}

  /// Clean up resources when the filter is closed.
  protected void cleanUp() throws IOException {}

  public void flushBlocks(boolean forceFlush) throws IOException {
    while(true) {
      var b = next;
      if(b.getState() == STATE_CLOSED) {
        var blen = b.getPosition() - b.getStart();
        b.unlinkOnly();
        if(blen != 0) filterBlock(b);
        else releaseBlock(b);
        if(b == this) break;
      } else if(flushPartial) {
        var blen = b.getPosition() - b.getStart();
        if(blen != 0) {
          filterBlock(b);
          b.setTotalFlushed(b.getTotalFlushed() + (b.getPosition() - b.getStart()));
          b.setPosition(b.getLimit());
          b.setStart(b.getLimit());
        }
        break;
      } else break;
    }
    if(forceFlush) flushPending();
  }

  /// Process the given non-empty closed block `b`. This method is called in output order for all
  /// closed blocks after unlinking them.
  ///
  /// A synchronous implementation of this method will do one of the following:
  /// - Write the filtered data directly to the [#parent] and release the block with
  ///   [#releaseBlock(BufferedOutput)].
  /// - Potentially request new blocks to hold output data with [#allocBlock()], append them to
  ///   the parent by calling [#appendBlockToParent(BufferedOutput)], and release any blocks that
  ///   were not appended to the parent with [#releaseBlock(BufferedOutput)].
  ///
  /// An asynchronous filter may instead start filtering the block on another thread, and collect
  /// any finished blocks to write them to the parent. It must also override [#flushPending()] to
  /// write pending blocks on demand. Note that all [BufferedOutput] methods may only be called
  /// on the main thread. In particular, you must not get a cached block from a background thread
  /// with [#allocBlock()]. Instead, wait for the next call to [#filterBlock(BufferedOutput)] or
  /// [#flushPending()] to get a new block and then continue processing, or use
  /// [#allocUncachedBlock()].
  ///
  /// Any block passed to [#filterBlock(BufferedOutput)] or allocated with [#allocBlock()] should
  /// either be passed to [#appendBlockToParent(BufferedOutput)] or released with
  /// [#releaseBlock(BufferedOutput)] at some point.
  ///
  /// If the filter is created with `flushPartial = true`, this method may also be called on
  /// an open block. Such a block must *not* be appended to the parent or released.
  ///
  /// Since the block was already unlinked before this method is called, an asynchronous
  /// implementation may use block links to keep a list of all pending blocks. It may also use
  /// [#filterState] to store additional information (for example,
  /// a [java.util.concurrent.CompletableFuture]).
  protected abstract void filterBlock(BufferedOutput b) throws IOException;

  /// This method is called on [#flush()] or [#close()]. The default implementation does nothing
  /// and is suitable for a synchronous filter implementation. An asynchronous filter must override
  /// it to wait until all blocks previously passed to [#filterBlock(BufferedOutput)] have been
  /// processed, and write them to the parent.
  protected void flushPending() throws IOException {}

  /// Release a block back to the cache.
  protected final void releaseBlock(BufferedOutput b) {
    b.setFilterState(null);
    if(b == this) {
      b = cachedBlock;
      b.setBuffer(buffer);
      cachedBlock = null;
    }
    cache.returnToCache((NestedBufferedOutput)b);
  }

  /// Get a block from the cache.
  protected final BufferedOutput allocBlock() { return cache.getExclusiveBlock(); }
  
  /// Allocate a new, uncached block. Unlike [#allocBlock()] this method is safe to call from
  /// other threads. It should not be used from the main thread.
  protected final BufferedOutput allocUncachedBlock() {
    var b = new NestedBufferedOutput(new byte[getInitialBufferSize()], false, this);
    b.setNocache(true);
    return b;
  }

  /// Append a block to the parent.
  protected final void appendBlockToParent(BufferedOutput b) throws IOException {
    int blen = b.getPosition() - b.getStart();
    if(parent.getRootBlock().totalBytesWritten() + blen > parent.getTotalLimit()) throw new EOFException();
    var emptyParent = parent.getPosition() == parent.getStart() && !parent.isFixed();
    if(emptyParent) {
      b.setFilterState(null);
      if(b == this) b = copyStateToCached();
      b.setRootBlock(parent.getRootBlock());
      b.setTopLevel(parent.getTopLevel());
      parent.getRootBlock().setTotalFlushed( blen + parent.getRootBlock().getTotalFlushed());
      b.insertBefore(parent);
      b.setState(STATE_CLOSED);
      if(b.getPrev() == parent.getTopLevel()) parent.getTopLevel().flushBlocks(false);
    } else if(parent.available() >= b.getPosition() - b.getStart() || parent.isFixed()) {
      var p = parent.fwd(blen);
      System.arraycopy(b.getBuffer(), b.getStart(), parent.getBuffer(), p, blen);
      releaseBlock(b);
    } else appendBlockToParentSwap(b);
  }

  private NestedBufferedOutput copyStateToCached() {
    var to = cachedBlock;
    to.setBuffer(this.buffer);
    to.setStart(this.start);
    to.setPosition(this.position);
    to.setLimit(this.limit);
    assert(this.sharing != SHARING_LEFT);
    to.setSharing(SHARING_EXCLUSIVE);
    to.setTotalFlushed(0);
    assert(this.state == STATE_CLOSED);
    clearLocal();
    return to;
  }
  
  private void clearLocal() {
    cachedBlock = null;
    buffer = null;
    prev = null;
    next = null;
  }

  private void appendBlockToParentSwap(BufferedOutput b) throws IOException {
    BufferedOutput bt;
    if(b == this) {
      bt = cachedBlock;
      clearLocal();
    } else bt = b;
    var tmpbuf = parent.getBuffer();
    var tmpstart = parent.getStart();
    var tmppos = parent.getPosition();
    var tmpsharing = parent.getSharing();
    var len = tmppos - tmpstart;
    parent.getRootBlock().setTotalFlushed(  len + parent.getRootBlock().getTotalFlushed());
    bt.setTotalFlushed(0);
    parent.setBuffer(b.getBuffer());
    parent.setStart(b.getStart());
    parent.setPosition(b.getPosition());
    parent.setLimit(parent.getBuffer().length);
    parent.setSharing(b.getSharing());
    bt.setBuffer(tmpbuf);
    bt.setStart(tmpstart);
    bt.setPosition(tmppos);
    bt.setSharing(tmpsharing);
    bt.setRootBlock(parent.getRootBlock());
    bt.setTopLevel(parent.getTopLevel());
    bt.insertBefore(parent);
    bt.setState(STATE_CLOSED);
    if(bt.getPrev() == parent.getTopLevel()) parent.getTopLevel().flushBlocks(false);
  }
}
