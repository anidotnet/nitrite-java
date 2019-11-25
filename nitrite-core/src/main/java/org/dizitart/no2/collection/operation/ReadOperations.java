package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.filters.FieldBasedFilter;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;

/**
 * @author Anindya Chatterjee
 */
class ReadOperations {
    private IndexOperations indexOperations;
    private NitriteMap<NitriteId, Document> nitriteMap;

    ReadOperations(IndexOperations indexOperations,
                   NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;
        this.indexOperations = indexOperations;
    }

    public DocumentCursor find() {
        Iterator<NitriteId> iterator = nitriteMap.keySet().iterator();
        return new DocumentCursorImpl(iterator, nitriteMap);
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null) {
            return find();
        }

        if (filter instanceof FieldBasedFilter) {
            FieldBasedFilter fieldBasedFilter = (FieldBasedFilter) filter;
            IndexEntry indexEntry = indexOperations.findIndexEntry(fieldBasedFilter.getField());
            Indexer indexer = indexOperations.findIndexer(indexEntry.getIndexType());
            fieldBasedFilter.setIndexer(indexer);
        }

        Iterator<KeyValuePair<NitriteId, Document>> entryIterator = nitriteMap.entries().iterator();
        Iterator<NitriteId> filteredIterator = new FilteredIterator(entryIterator, filter);

        return new DocumentCursorImpl(filteredIterator, nitriteMap);
    }

    Document getById(NitriteId nitriteId) {
        return nitriteMap.get(nitriteId);
    }
}
