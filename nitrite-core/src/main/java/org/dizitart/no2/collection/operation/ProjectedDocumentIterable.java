package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentIterable implements ReadableStream<Document> {
    private final ReadableStream<NitriteId> readableStream;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private Document projection;

    public ProjectedDocumentIterable(ReadableStream<NitriteId> readableStream,
                                     NitriteMap<NitriteId, Document> nitriteMap,
                                     Document projection) {
        this.readableStream = readableStream;
        this.nitriteMap = nitriteMap;
        this.projection = projection;
    }

    @Override
    public Iterator<Document> iterator() {
        Iterator<NitriteId> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new ProjectedDocumentIterator(iterator);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;
        private Document nextElement = null;

        ProjectedDocumentIterator(Iterator<NitriteId> iterator) {
            this.iterator = iterator;
            nextMatch();
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @Override
        public Document next() {
            Document returnValue = nextElement;
            nextMatch();
            return returnValue;
        }

        private void nextMatch() {
            while (iterator.hasNext()) {
                NitriteId next = iterator.next();
                Document document = nitriteMap.get(next);
                if (document != null) {
                    Document projected = project(document.clone());
                    if (projected != null) {
                        nextElement = projected;
                        return;
                    }
                }
            }

            nextElement = null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }

        private Document project(Document original) {
            if (projection == null) return original;
            Document result = original.clone();

            for (KeyValuePair<String, Object> keyValuePair : original) {
                if (!projection.containsKey(keyValuePair.getKey())) {
                    result.remove(keyValuePair.getKey());
                }
            }
            return result;
        }
    }
}

