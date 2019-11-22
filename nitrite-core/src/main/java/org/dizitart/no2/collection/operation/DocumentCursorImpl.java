package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.Iterator;
import java.util.TreeSet;

import static org.dizitart.no2.exceptions.ErrorMessage.PROJECTION_WITH_NOT_NULL_VALUES;
import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursorImpl implements DocumentCursor {
    private final ReadableStream<NitriteId> resultSet;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private boolean hasMore;
    private long totalCount;
    private FindResult findResult;

    DocumentCursorImpl(FindResult findResult) {
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = ReadableStream.fromIterable(new TreeSet<>());
        }
        this.nitriteMap = findResult.getNitriteMap();
        this.hasMore = findResult.getHasMore();
        this.totalCount = findResult.getTotalCount();
        this.findResult = findResult;
    }

    @Override
    public RecordIterable<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentIterable(projection, findResult);
    }

    @Override
    public RecordIterable<Document> join(DocumentCursor cursor, Lookup lookup) {
        return new JoinedDocumentIterable(findResult, cursor, lookup);
    }

    @Override
    public ReadableStream<NitriteId> idSet() {
        return resultSet;
    }

    @Override
    public Iterator<Document> iterator() {
        return new DocumentCursorIterator();
    }

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public long size() {
        return resultSet.size();
    }

    @Override
    public long totalCount() {
        return totalCount;
    }

    private class DocumentCursorIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;

        DocumentCursorIterator() {
            iterator = resultSet.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Document next() {
            NitriteId next = iterator.next();
            Document document = nitriteMap.get(next);
            if (document != null) {
                return document.clone();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED);
        }
    }

    private void validateProjection(Document projection) {
        for (KeyValuePair<String, Object> kvp : projection) {
            validateKeyValuePair(kvp);
        }
    }

    private void validateKeyValuePair(KeyValuePair<String, Object> kvp) {
        if (kvp.getValue() != null) {
            if (!(kvp.getValue() instanceof Document)) {
                throw new ValidationException(PROJECTION_WITH_NOT_NULL_VALUES);
            } else {
                validateProjection((Document) kvp.getValue());
            }
        }
    }
}