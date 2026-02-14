package com.dastan.core.io.adapter;

import java.io.IOException;

/// An iterator over a list of byte array buffers. It is initially positioned before the first
/// buffer. Calling [#next()] advances it to the next buffer. All buffers must be non-empty. An
/// empty iterator contains no buffers.
/// 
/// After a buffer has been handed over with [#next()] it may be modified by the consumer, both
/// inside and outside the bounds indicated by [#start()] and [#end()]. Consumers should return
/// buffers after use with [#returnBuffer(Object)] so they can be reused more efficiently than
/// having to garbage-collect and reallocate them.
public abstract class BufferIterator {

  /// Advance to the next non-empty buffer and return a buffer id, or null if the end of the list
  /// has been reached.
  public abstract Object next() throws IOException;

  /// The current buffer.
  /// The behavior is undefined before successfully retrieving a buffer with [#next()].
  public abstract byte[] buffer();
  
  /// The first used index in the current buffer.
  /// The behavior is undefined before successfully retrieving a buffer with [#next()].
  public abstract int start();
  
  /// The index after the last used index in the current buffer.
  /// The behavior is undefined before successfully retrieving a buffer with [#next()].
  public abstract int end();
  
  /// The number of bytes in the current buffer, equivalent to `end() - start()`
  /// The behavior is undefined before successfully retrieving a buffer with [#next()].
  public int length() { return end() - start(); }

  /// Return the buffer with the given id so it can be reused py the producer.
  /// Buffers may be returned out of order.
  public abstract void returnBuffer(Object id);
}


