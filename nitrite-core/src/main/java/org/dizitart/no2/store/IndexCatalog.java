package org.dizitart.no2.store;

import org.dizitart.no2.collection.index.IndexEntry;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
public interface IndexCatalog {
    boolean hasIndexEntry(String collectionName, String field);

    IndexEntry createIndexEntry(String field, String indexType);

    IndexEntry findIndexEntry(String collectionName, String field);

    boolean isDirtyIndex(String collectionName, String field);

    Collection<IndexEntry> listIndexEntries(String collectionName);

    void dropIndexEntry(String collectionName, String field);

    void beginIndexing(String collectionName, String field);

    void endIndexing(String collectionName, String field);
}
