package org.dizitart.no2.repository;

import org.dizitart.no2.Document;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Iterator;

import static org.dizitart.no2.common.Constants.DOC_ID;

/**
 * @author Anindya Chatterjee.
 */
class JoinedObjectIterable<T> implements ReadableStream<T> {
    private ReadableStream<Document> recordIterable;
    private Class<T> joinType;
    private NitriteMapper nitriteMapper;

    JoinedObjectIterable(NitriteMapper nitriteMapper,
                         ReadableStream<Document> recordIterable,
                         Class<T> joinType) {
        this.recordIterable = recordIterable;
        this.joinType = joinType;
        this.nitriteMapper = nitriteMapper;
    }

    @Override
    public Iterator<T> iterator() {
        return new JoinedObjectIterator(nitriteMapper);
    }

    private class JoinedObjectIterator implements Iterator<T> {
        private NitriteMapper objectMapper;
        private Iterator<Document> documentIterator;

        JoinedObjectIterator(NitriteMapper nitriteMapper) {
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
                return objectMapper.asObject(record, joinType);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }
    }
}
