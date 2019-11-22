package org.dizitart.no2.collection.index;

import org.dizitart.no2.collection.Field;

/**
 * @author Anindya Chatterjee
 */
public interface IndexedQueryTemplate {
    boolean hasIndex(Field field);

    boolean isIndexing(Field field);

    IndexEntry findIndex(Field field);

    Indexer getIndexer(String indexType);
}
