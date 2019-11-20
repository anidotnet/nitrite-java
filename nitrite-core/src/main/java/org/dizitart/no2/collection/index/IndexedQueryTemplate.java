package org.dizitart.no2.collection.index;

/**
 * @author Anindya Chatterjee
 */
public interface IndexedQueryTemplate {
    boolean hasIndex(String field);

    boolean isIndexing(String field);

    IndexEntry findIndex(String field);

    Indexer getIndexer(String indexType);
}
