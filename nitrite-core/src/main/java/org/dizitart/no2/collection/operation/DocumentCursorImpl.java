package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;

import static org.dizitart.no2.exceptions.ErrorMessage.PROJECTION_WITH_NOT_NULL_VALUES;
import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursorImpl implements DocumentCursor {
    private final Iterator<NitriteId> iterator;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    DocumentCursorImpl(Iterator<NitriteId> iterator, NitriteMap<NitriteId, Document> nitriteMap) {
        this.iterator = iterator;
        this.nitriteMap = nitriteMap;
    }

    @Override
    public ReadableStream<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentIterable(iterator, nitriteMap, projection);
    }

    @Override
    public ReadableStream<Document> join(DocumentCursor cursor, Lookup lookup) {
        return new JoinedDocumentIterable(iterator, nitriteMap, cursor, lookup);
    }

    @Override
    public Iterator<Document> iterator() {
        return new DocumentCursorIterator(iterator);
    }

    private class DocumentCursorIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;

        DocumentCursorIterator(Iterator<NitriteId> iterator) {
            this.iterator = iterator;
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
