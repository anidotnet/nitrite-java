package org.dizitart.no2.repository;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Iterator;

import static org.dizitart.no2.common.Constants.DOC_ID;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedObjectIterable<T> implements ReadableStream<T> {
    private ReadableStream<Document> recordIterable;
    private Class<T> projectionType;
    private NitriteMapper nitriteMapper;

    ProjectedObjectIterable(NitriteMapper nitriteMapper,
                            ReadableStream<Document> recordIterable,
                            Class<T> projectionType) {
        this.recordIterable = recordIterable;
        this.projectionType = projectionType;
        this.nitriteMapper = nitriteMapper;
    }

    @Override
    public Iterator<T> iterator() {
        return new ProjectedObjectIterator(nitriteMapper);
    }

    private class ProjectedObjectIterator implements Iterator<T> {
        private NitriteMapper objectMapper;
        private Iterator<Document> documentIterator;

        ProjectedObjectIterator(NitriteMapper nitriteMapper) {
            this.objectMapper = nitriteMapper;
            this.documentIterator = recordIterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document item = documentIterator.next();
            if (item != null) {
                Document record = item.clone();
                record.remove(DOC_ID);
                return objectMapper.convertType(record, projectionType);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }
    }
}
