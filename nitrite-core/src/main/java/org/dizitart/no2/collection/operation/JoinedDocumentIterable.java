package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
class JoinedDocumentIterable implements ReadableStream<Document> {
    private final ReadableStream<NitriteId> readableStream;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private DocumentCursor foreignCursor;
    private Lookup lookup;

    JoinedDocumentIterable(ReadableStream<NitriteId> readableStream,
                           NitriteMap<NitriteId, Document> nitriteMap,
                           DocumentCursor foreignCursor,
                           Lookup lookup) {
        this.readableStream = readableStream;
        this.nitriteMap = nitriteMap;
        this.foreignCursor = foreignCursor;
        this.lookup = lookup;
    }


    @Override
    public Iterator<Document> iterator() {
        return new JoinedDocumentIterator(readableStream.iterator());
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class JoinedDocumentIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;

        JoinedDocumentIterator(Iterator<NitriteId> iterator) {
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
                return join(document.clone(), foreignCursor, lookup);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }

        private Document join(Document localDocument, DocumentCursor foreignCursor, Lookup lookup) {
            Object localObject = localDocument.get(lookup.getLocalField());
            if (localObject == null) return localDocument;
            Set<Document> target = new HashSet<>();

            for (Document foreignDocument: foreignCursor) {
                Object foreignObject = foreignDocument.get(lookup.getForeignField());
                if (foreignObject != null) {
                    if (deepEquals(foreignObject, localObject)) {
                        target.add(foreignDocument);
                    }
                }
            }
            if (!target.isEmpty()) {
                localDocument.put(lookup.getTargetField(), target);
            }
            return localDocument;
        }
    }
}

