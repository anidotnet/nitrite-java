package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.filters.NitriteFilter;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;

import java.util.Iterator;

/**
 * @author Anindya Chatterjee
 */
class ReadOperations {
    private String collectionName;
    private NitriteConfig nitriteConfig;
    private NitriteMap<NitriteId, Document> nitriteMap;

    ReadOperations(String collectionName,
                   NitriteConfig nitriteConfig,
                   NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.collectionName = collectionName;
    }

    public DocumentCursor find() {
        Iterator<NitriteId> iterator = nitriteMap.keySet().iterator();
        return new DocumentCursorImpl(iterator, nitriteMap);
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null) {
            return find();
        }

        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            nitriteFilter.setNitriteConfig(nitriteConfig);
            nitriteFilter.setCollectionName(collectionName);
        }

        Iterator<KeyValuePair<NitriteId, Document>> entryIterator = nitriteMap.entries().iterator();
        Iterator<NitriteId> filteredIterator = new FilteredIterator(entryIterator, filter);

        return new DocumentCursorImpl(filteredIterator, nitriteMap);
    }

    Document getById(NitriteId nitriteId) {
        return nitriteMap.get(nitriteId);
    }
}
