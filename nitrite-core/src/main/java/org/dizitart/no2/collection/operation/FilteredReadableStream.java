package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.filters.Filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee.
 */
class FilteredReadableStream implements ReadableStream<NitriteId> {
    private ReadableStream<KeyValuePair<NitriteId, Document>> readableStream;
    private Filter filter;

    FilteredReadableStream(ReadableStream<KeyValuePair<NitriteId, Document>> readableStream, Filter filter) {
        this.readableStream = readableStream;
        this.filter = filter;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        Iterator<KeyValuePair<NitriteId, Document>> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new FilteredIterator(iterator, filter);
    }

    static class FilteredIterator implements Iterator<NitriteId> {
        private Iterator<KeyValuePair<NitriteId, Document>> iterator;
        private Filter filter;
        private NitriteId nextId;
        private boolean nextIdSet = false;

        public FilteredIterator(Iterator<KeyValuePair<NitriteId, Document>> iterator, Filter filter) {
            this.iterator = iterator;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return nextIdSet || setNextId();
        }

        @Override
        public NitriteId next() {
            if (!nextIdSet && !setNextId()) {
                throw new NoSuchElementException();
            }
            nextIdSet = false;
            return nextId;
        }

        @Override
        public void remove() {
            if (nextIdSet) {
                throw new InvalidOperationException("remove operation cannot be called here");
            }
            iterator.remove();
        }

        private boolean setNextId() {
            while (iterator.hasNext()) {
                final KeyValuePair<NitriteId, Document> keyValuePair = iterator.next();
                if (filter.apply(keyValuePair)) {
                    nextId = keyValuePair.getKey();
                    nextIdSet = true;
                    return true;
                }
            }
            return false;
        }
    }

}
