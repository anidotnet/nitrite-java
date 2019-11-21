package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;
import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class JoinedDocumentIterable implements RecordIterable<Document> {
    private final ReadableStream<NitriteId> resultSet;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private boolean hasMore;
    private long totalCount;
    private DocumentCursor foreignCursor;
    private Lookup lookup;

    JoinedDocumentIterable(FindResult findResult, DocumentCursor foreignCursor, Lookup lookup) {
        this.foreignCursor = foreignCursor;
        this.lookup = lookup;
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
    public Iterator<Document> iterator() {
        return new JoinedDocumentIterator();
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class JoinedDocumentIterator implements Iterator<Document> {
        private Iterator<NitriteId> iterator;

        JoinedDocumentIterator() {
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
                return join(document.clone(), foreignCursor, lookup);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED);
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

