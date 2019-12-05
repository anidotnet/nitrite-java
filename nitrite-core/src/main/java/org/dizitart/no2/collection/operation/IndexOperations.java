package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.util.ValidationUtils.validateDocumentIndexField;

/**
 * @author Anindya Chatterjee
 */
class IndexOperations {
    private String collectionName;
    private NitriteConfig nitriteConfig;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private IndexCatalog indexCatalog;
    private Map<String, AtomicBoolean> indexBuildRegistry;
    private ExecutorService rebuildExecutor;

    IndexOperations(NitriteConfig nitriteConfig, NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        init();
    }

    boolean isIndexing(String field) {
        // has index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexCatalog.hasIndexEntry(collectionName, field)
            && indexBuildRegistry.get(field) != null
            && indexBuildRegistry.get(field).get();
    }

    boolean hasIndexEntry(String field) {
        return indexCatalog.hasIndexEntry(collectionName, field);
    }

    void ensureIndex(String field, String indexType, boolean isAsync) {
        IndexEntry indexEntry;
        if (!hasIndexEntry(field)) {
            // if no index create index
            indexEntry = indexCatalog.createIndexEntry(collectionName, field, indexType);
        } else {
            // if index already there throw
            throw new IndexingException("index already exists on " + field);
        }

        rebuildIndex(indexEntry, isAsync);
    }

    void updateIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fieldNames = document.getFields();
        for (String field : fieldNames) {
            IndexEntry indexEntry = findIndexEntry(field);
            if (indexEntry != null) {
                Object fieldValue = document.get(field);
                if (fieldValue == null) continue;
                validateDocumentIndexField(fieldValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexCatalog.isDirtyIndex(collectionName, field)
                    && indexBuildRegistry.get(field) != null
                    && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(indexEntry, true);
                } else {
                    String indexType = indexEntry.getIndexType();
                    Indexer indexer = findIndexer(indexType);
                    indexer.writeIndex(nitriteMap, nitriteId, field, fieldValue);
                }
            }
        }
    }

    void removeIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fieldNames = document.getFields();
        for (String field : fieldNames) {
            IndexEntry indexEntry = findIndexEntry(field);
            if (indexEntry != null) {
                Object fieldValue = document.get(field);

                if (fieldValue == null) continue;
                validateDocumentIndexField(fieldValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexCatalog.isDirtyIndex(collectionName, field)
                    && indexBuildRegistry.get(field) != null
                    && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(indexEntry, true);
                } else {
                    String indexType = indexEntry.getIndexType();
                    Indexer indexer = findIndexer(indexType);
                    indexer.removeIndex(nitriteMap, nitriteId, field, fieldValue);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    void refreshIndexEntry(Document oldDocument, Document newDocument, NitriteId nitriteId) {
        Set<String> fieldNames = newDocument.getFields();
        for (String field : fieldNames) {
            IndexEntry indexEntry = findIndexEntry(field);
            if (indexEntry != null) {
                Object newValue = newDocument.get(field);
                Object oldValue = oldDocument.get(field);

                if (newValue == null) continue;
                if (newValue instanceof Comparable && oldValue instanceof Comparable) {
                    if (((Comparable) newValue).compareTo(oldValue) == 0) continue;
                }

                validateDocumentIndexField(newValue, field);

                if (indexCatalog.isDirtyIndex(collectionName, field)
                    && indexBuildRegistry.get(field) != null
                    && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(indexEntry, true);
                } else {
                    String indexType = indexEntry.getIndexType();
                    Indexer indexer = findIndexer(indexType);
                    indexer.updateIndex(nitriteMap, nitriteId, field, newValue, oldValue);
                }
            }
        }
    }

    void dropIndex(String field) {
        if (indexBuildRegistry.get(field) != null
            && indexBuildRegistry.get(field).get()) {
            throw new IndexingException("cannot drop index as indexing is running on " + field);
        }

        IndexEntry indexEntry = findIndexEntry(field);
        if (indexEntry != null) {
            String indexType = indexEntry.getIndexType();
            Indexer indexer = findIndexer(indexType);
            indexer.dropIndex(nitriteMap, field);
            indexCatalog.dropIndexEntry(collectionName, field);
            indexBuildRegistry.remove(field);
        } else {
            throw new IndexingException(field + " is not indexed");
        }
    }

    void dropAllIndices() {
        for (Map.Entry<String, AtomicBoolean> entry :indexBuildRegistry.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException("cannot drop index as indexing is running on " + entry.getKey());
            }
        }

        for (IndexEntry index : listIndexes()) {
            dropIndex(index.getField());
        }
        indexBuildRegistry.clear();
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void rebuildIndex(IndexEntry indexEntry, boolean isAsync) {
        final String field = indexEntry.getField();
        if (getBuildFlag(field).compareAndSet(false, true)) {
            if (isAsync) {
                rebuildExecutor.submit(() -> buildIndexInternal(field, indexEntry));
            } else {
                buildIndexInternal(field, indexEntry);
            }
            return;
        }
        throw new IndexingException("indexing is already running on " + indexEntry.getField());
    }

    Collection<IndexEntry> listIndexes() {
        return indexCatalog.listIndexEntries(collectionName);
    }

    IndexEntry findIndexEntry(String field) {
        return indexCatalog.findIndexEntry(collectionName, field);
    }

    private Indexer findIndexer(String indexType) {
        Indexer indexer = nitriteConfig.findIndexer(indexType);
        if (indexer != null) {
            return indexer;
        }
        throw new IndexingException("no indexer found for index type " + indexType);
    }

    private void init() {
        NitriteStore nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = nitriteStore.getIndexCatalog();
        this.collectionName = nitriteMap.getName();
        this.indexBuildRegistry = new ConcurrentHashMap<>();
        this.rebuildExecutor = ExecutorServiceManager.commonPool();
    }

    private void buildIndexInternal(final String field, final IndexEntry indexEntry) {
        try {
            // first put dirty marker
            indexCatalog.beginIndexing(collectionName, field);

            String indexType = indexEntry.getIndexType();
            Indexer indexer = findIndexer(indexType);
            indexer.rebuildIndex(nitriteMap, field);
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexCatalog.endIndexing(collectionName, field);
            getBuildFlag(field).set(false);
        }
    }

    private AtomicBoolean getBuildFlag(String field) {
        AtomicBoolean flag = indexBuildRegistry.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildRegistry.put(field, flag);
        return flag;
    }
}
