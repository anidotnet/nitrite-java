package org.dizitart.no2.collection.operation;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anindya Chatterjee
 */
public class CollectionOperation {
    private NitriteConfig nitriteConfig;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private IndexTemplate indexTemplate;
    private ReadWriteOperation readWriteOperation;
    private QueryTemplate queryTemplate;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private Lock readLock;
    private Lock writeLock;

    @Getter
    @Setter
    private Attributes attributes;

    public CollectionOperation(NitriteMap<NitriteId, Document> nitriteMap,
                               NitriteConfig nitriteConfig,
                               EventBus<ChangedItem<Document>, ChangeListener> eventBus) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.eventBus = eventBus;
        init();
    }

    public void createIndex(String field, String indexType, boolean async) {
        try {
            writeLock.lock();
            indexTemplate.ensureIndex(field, indexType, async);
        } finally {
            writeLock.unlock();
        }
    }

    public IndexEntry findIndex(String field) {
        try {
            readLock.lock();
            return indexTemplate.findIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public void rebuildIndex(IndexEntry indexEntry, boolean async) {
        try {
            writeLock.lock();
            indexTemplate.rebuildIndex(indexEntry, async);
        } finally {
            writeLock.unlock();
        }
    }

    public Collection<IndexEntry> listIndexes() {
        try {
            readLock.lock();
            return indexTemplate.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(String field) {
        try {
            readLock.lock();
            return indexTemplate.hasIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(String field) {
        try {
            readLock.lock();
            return indexTemplate.isIndexing(field);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(String field) {
        try {
            writeLock.lock();
            indexTemplate.dropIndex(field);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        try {
            writeLock.lock();
            indexTemplate.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult insert(Document[] documents) {
        try {
            writeLock.lock();
            return readWriteOperation.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        try {
            writeLock.lock();
            return readWriteOperation.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        try {
            writeLock.lock();
            return readWriteOperation.remove(filter, removeOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find() {
        try {
            readLock.lock();
            return queryTemplate.find();
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter) {
        try {
            readLock.lock();
            return queryTemplate.find(filter);
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(FindOptions findOptions) {
        try {
            readLock.lock();
            return queryTemplate.find(findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            return queryTemplate.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public Document getById(NitriteId nitriteId) {
        try {
            readLock.lock();
            return queryTemplate.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    public void dropCollection() {
        try {
            writeLock.lock();
            indexTemplate.dropAllIndices();
            nitriteMap.drop();
        } finally {
            writeLock.unlock();
        }
    }

    public long getSize() {
        try {
            readLock.lock();
            return nitriteMap.size();
        } finally {
            readLock.unlock();
        }
    }

    public void close() {
        try {
            writeLock.lock();
            nitriteMap.close();
        } finally {
            writeLock.unlock();
        }
    }

    private void init() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.indexTemplate = new IndexTemplate(nitriteConfig, nitriteMap);
        this.queryTemplate = new QueryTemplate(indexTemplate, nitriteMap);
        this.readWriteOperation = new ReadWriteOperation(indexTemplate, queryTemplate,
            nitriteMap, eventBus);
    }
}
