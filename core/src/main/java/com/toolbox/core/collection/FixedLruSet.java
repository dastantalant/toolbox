package com.toolbox.core.collection;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Least Recently Used
 */
public class FixedLruSet<E> {
    private final Set<E> delegate;
    public FixedLruSet(int maxSize) {delegate = newFixedLruSet(maxSize);}
    public static <E> Set<E> newFixedLruSet(int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("maxSize must be positive");
        LinkedHashMap<E, Boolean> map = new LinkedHashMap<E, Boolean>(maxSize + 1, 1.0f, true) {
            private static final long serialVersionUID = 1L;
            @Override protected boolean removeEldestEntry(Map.Entry<E, Boolean> eldest) {return size() > maxSize;}
        };
        return Collections.newSetFromMap(map);
    }
    public boolean add(E e) {return delegate.add(e);}
    public boolean remove(Object o) {return delegate.remove(o);}
    public boolean contains(Object o) {return delegate.contains(o);}
    public int size() {return delegate.size();}
    public void clear() {delegate.clear();}
    public Iterator<E> iterator() {return delegate.iterator();}
}
