package org.dizitart.no2.collection.index;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.plugin.NitritePlugin;

/**
 * @author Anindya Chatterjee.
 */
public interface Indexer extends NitritePlugin {
    String getIndexType();

    void writeIndex(String collectionName, NitriteId nitriteId, String field, Object fieldValue);

    void removeIndex(String collectionName, NitriteId nitriteId, String field, Object fieldValue);

    void updateIndex(String collectionName, NitriteId nitriteId, String field, Object newValue, Object oldValue);

    void dropIndex(String collectionName, String field);

    void rebuildIndex(String collectionName, String field);
}
