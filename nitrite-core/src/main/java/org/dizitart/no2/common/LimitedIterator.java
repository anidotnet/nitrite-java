package org.dizitart.no2.common;

import org.dizitart.no2.exceptions.ValidationException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee.
 */
public class LimitedIterator<T> implements Iterator<T> {
    private final Iterator<? extends T> iterator;
    private final int offset;
    private final int size;
    private long pos;

    public LimitedIterator(final Iterator<? extends T> iterator, final int offset, final int size) {
        if (iterator == null) {
            throw new ValidationException("iterator must not be null");
        }
        if (offset < 0) {
            throw new ValidationException("offset parameter must not be negative.");
        }
        if (size < 0) {
            throw new ValidationException("size parameter must not be negative.");
        }

        this.iterator = iterator;
        this.offset = offset;
        this.size = size;
        pos = 0;
        init();
    }

    private void init() {
        while (pos < offset && iterator.hasNext()) {
            iterator.next();
            pos++;
        }
    }

    @Override
    public boolean hasNext() {
        if (!checkBounds()) {
            return false;
        }
        return iterator.hasNext();
    }

    private boolean checkBounds() {
        return pos - offset + 1 <= size;
    }

    @Override
    public T next() {
        if (!checkBounds()) {
            throw new NoSuchElementException();
        }
        final T next = iterator.next();
        pos++;
        return next;
    }

    @Override
    public void remove() {
        if (pos <= offset) {
            throw new IllegalStateException("remove() cannot be called before calling next()");
        }
        iterator.remove();
    }
}
