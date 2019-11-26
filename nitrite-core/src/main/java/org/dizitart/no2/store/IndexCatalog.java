package org.dizitart.no2.store;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.IndexEntry;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Anindya Chatterjee
 */
public interface IndexCatalog {
    boolean hasIndexEntry(String collectionName, Field field);

    IndexEntry createIndexEntry(String collectionName, Field field, String indexType);

    IndexEntry findIndexEntry(String collectionName, Field field);

    boolean isDirtyIndex(String collectionName, Field field);

    Collection<IndexEntry> listIndexEntries(String collectionName);

    void dropIndexEntry(String collectionName, Field field);

    void beginIndexing(String collectionName, Field field);

    void endIndexing(String collectionName, Field field);

    NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String collectionName, Field field);
}
