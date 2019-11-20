package org.dizitart.no2.collection.index;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.store.NitriteMap;

/**
 * @author Anindya Chatterjee.
 */
public interface Indexer extends NitritePlugin {
    String getIndexType();

    void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue);

    void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue);

    void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue);

    void dropIndex(NitriteMap<NitriteId, Document> collection, String field);

    void rebuildIndex(NitriteMap<NitriteId, Document> collection, String field);
}
