package org.dizitart.no2.collection.index;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.store.NitriteMap;

import static org.dizitart.no2.common.Constants.INDEX_PREFIX;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;

/**
 * @author Anindya Chatterjee.
 */
public interface Indexer extends NitritePlugin {
    String getIndexType();

    void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object fieldValue);

    void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object fieldValue);

    void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object newValue, Object oldValue);

    void dropIndex(NitriteMap<NitriteId, Document> collection, Field field);

    void rebuildIndex(NitriteMap<NitriteId, Document> collection, Field field);

    default String getIndexMapName(String collectionName, Field field) {
        return INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            collectionName +
            INTERNAL_NAME_SEPARATOR +
            field.getName() +
            INTERNAL_NAME_SEPARATOR +
            getIndexType();
    }
}
