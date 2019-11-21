package org.dizitart.no2.collection.index;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

/**
 * @author Anindya Chatterjee.
 */
public class UniqueIndexer implements Indexer {
    @Override
    public String getIndexType() {
        return IndexType.Unique;
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {

    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {

    }

    @Override
    public void rebuildIndex(NitriteMap<NitriteId, Document> collection, String field) {

    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }
}
