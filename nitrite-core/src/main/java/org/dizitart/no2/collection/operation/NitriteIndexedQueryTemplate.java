package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.index.IndexedQueryTemplate;
import org.dizitart.no2.collection.index.Indexer;

/**
 * @author Anindya Chatterjee
 */
class NitriteIndexedQueryTemplate implements IndexedQueryTemplate {
    private IndexTemplate indexTemplate;

    NitriteIndexedQueryTemplate(IndexTemplate indexTemplate) {
        this.indexTemplate = indexTemplate;
    }

    @Override
    public boolean hasIndex(Field field) {
        return indexTemplate.hasIndexEntry(field);
    }

    @Override
    public boolean isIndexing(Field field) {
        return indexTemplate.isIndexing(field);
    }

    @Override
    public IndexEntry findIndex(Field field) {
        return indexTemplate.findIndexEntry(field);
    }

    @Override
    public Indexer getIndexer(String indexType) {
        return indexTemplate.findIndexer(indexType);
    }
}
