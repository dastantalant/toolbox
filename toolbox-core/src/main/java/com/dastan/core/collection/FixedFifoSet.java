package com.dastan.core.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
/**
 * First In, First Out
 */
public class FixedFifoSet<E> extends LinkedHashSet<E> {
    private final int maxSize;

    public FixedFifoSet(int maxSize) {
        super(maxSize + 1);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        trimToSize();
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = super.addAll(c);
        trimToSize();
        return modified;
    }

    private void trimToSize() {
        while (size() > maxSize) {
            Iterator<E> it = iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }
}