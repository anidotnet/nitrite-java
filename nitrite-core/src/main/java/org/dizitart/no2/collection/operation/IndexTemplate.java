package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.index.Index;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.IE_INVALID_INDEX_TYPE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
class IndexTemplate {
    private String collectionName;
    private NitriteConfig nitriteConfig;
    private NitriteStore nitriteStore;
    private Map<String, Indexer> indexerMap;

    public IndexTemplate(String collectionName, NitriteConfig nitriteConfig, NitriteStore nitriteStore) {
        this.collectionName = collectionName;
        this.nitriteConfig = nitriteConfig;
        this.nitriteStore = nitriteStore;
        init();
    }

    public void ensureIndex(String field, String indexType, boolean async) {

    }

    public void rebuildIndex(Index index, boolean async) {

    }

    public Index findIndex(String field) {
        return null;
    }

    public boolean isIndexing(String field) {
        return false;
    }

    public boolean hasIndex(String field) {
        return false;
    }

    public void dropAllIndices() {

    }

    public Collection<Index> listIndexes() {
        return null;
    }

    public void dropIndex(String field) {

    }

    private void init() {
        indexerMap = new HashMap<>();
        Set<Indexer> indexers = nitriteConfig.getIndexers();
        for (Indexer indexer : indexers) {
            indexerMap.put(indexer.getIndexType(), indexer);
        }
    }

    private Indexer findIndexer(String indexType) {
        if (indexerMap.containsKey(indexType)) {
            return indexerMap.get(indexType);
        }
        throw new IndexingException(errorMessage("no indexer found for index type " + indexType,
            IE_INVALID_INDEX_TYPE));
    }
}
