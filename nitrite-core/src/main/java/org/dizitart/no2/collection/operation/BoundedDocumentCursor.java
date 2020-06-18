package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.ValidationException;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee.
 */
class BoundedDocumentCursor implements ReadableStream<NitriteId> {
    private final ReadableStream<NitriteId> readableStream;
    private final long offset;
    private final long limit;

    BoundedDocumentCursor(ReadableStream<NitriteId> readableStream, final long offset, final long limit) {
        if (offset < 0) {
            throw new ValidationException("offset parameter must not be negative");
        }
        if (limit < 0) {
            throw new ValidationException("limit parameter must not be negative");
        }

        this.readableStream = readableStream;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        Iterator<NitriteId> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new BoundedIterator<>(iterator, offset, limit);
    }

    public static class BoundedIterator<T> implements Iterator<T> {
        private final Iterator<? extends T> iterator;
        private final long offset;
        private final long size;
        private long pos;

        public BoundedIterator(final Iterator<? extends T> iterator, final long offset, final long size) {
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
            if (checkBounds()) {
                return false;
            }
            return iterator.hasNext();
        }

        private boolean checkBounds() {
            return pos - offset + 1 > size;
        }

        @Override
        public T next() {
            if (checkBounds()) {
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

}
