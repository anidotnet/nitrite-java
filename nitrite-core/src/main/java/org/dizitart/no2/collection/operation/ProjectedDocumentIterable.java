package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.Iterator;
import java.util.TreeSet;

import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentIterable implements RecordIterable<Document> {
    private final ReadableStream<NitriteId> resultSet;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private Document projection;
    private boolean hasMore;
    private long totalCount;
    private FindResult findResult;

    ProjectedDocumentIterable(Document projection, FindResult findResult) {
        this.findResult = findResult;
        this.projection = projection;
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = ReadableStream.fromIterable(new TreeSet<>());
        }
        this.nitriteMap = findResult.getNitriteMap();
        this.hasMore = findResult.getHasMore();
        this.totalCount = findResult.getTotalCount();
    }

    @Override
    public Iterator<Document> iterator() {
        return new ProjectedDocumentIterator();
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

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;
        private Document nextElement = null;

        ProjectedDocumentIterator() {
            iterator = resultSet.iterator();
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
            throw new InvalidOperationException(REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED);
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

