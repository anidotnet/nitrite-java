package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.common.KeyValuePair;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anindya Chatterjee
 */
class FilteredIterator implements Iterator<NitriteId> {
    private Iterator<KeyValuePair<NitriteId, Document>> iterator;
    private Filter filter;
    private NitriteId nextId;
    private boolean nextIdSet = false;

    public FilteredIterator(Iterator<KeyValuePair<NitriteId, Document>> iterator) {
        this.iterator = iterator;
    }

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
            throw new IllegalStateException("remove() cannot be called");
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
