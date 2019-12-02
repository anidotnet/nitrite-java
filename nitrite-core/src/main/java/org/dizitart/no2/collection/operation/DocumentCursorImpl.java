package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;

import java.text.Collator;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursorImpl implements DocumentCursor {
    private final ReadableStream<NitriteId> readableStream;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    DocumentCursorImpl(ReadableStream<NitriteId> readableStream, NitriteMap<NitriteId, Document> nitriteMap) {
        this.readableStream = readableStream;
        this.nitriteMap = nitriteMap;
    }

    @Override
    public DocumentCursor sort(Field field, SortOrder sortOrder, Collator collator, NullOrder nullOrder) {
        return new DocumentCursorImpl(new SortedDocumentCursor(field, sortOrder, collator,
            nullOrder, readableStream, nitriteMap), nitriteMap);
    }

    @Override
    public DocumentCursor limit(int offset, int size) {
        return new DocumentCursorImpl(new LimitedDocumentCursor(readableStream, offset, size), nitriteMap);
    }

    @Override
    public ReadableStream<Document> project(Document projection) {
        validateProjection(projection);
        return new ProjectedDocumentIterable(readableStream, nitriteMap, projection);
    }

    @Override
    public ReadableStream<Document> join(DocumentCursor cursor, Lookup lookup) {
        return new JoinedDocumentIterable(readableStream, nitriteMap, cursor, lookup);
    }

    @Override
    public Iterator<Document> iterator() {
        return new DocumentCursorIterator(readableStream.iterator());
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
            throw new InvalidOperationException("remove on cursor is not supported");
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
                throw new ValidationException("projection contains non-null values");
            } else {
                validateProjection((Document) kvp.getValue());
            }
        }
    }
}
