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
public class CollectionOperations {
    private NitriteConfig nitriteConfig;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private IndexOperations indexOperations;
    private WriteOperations writeOperations;
    private ReadOperations readOperations;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private Lock readLock;
    private Lock writeLock;

    @Getter
    @Setter
    private Attributes attributes;

    public CollectionOperations(NitriteMap<NitriteId, Document> nitriteMap,
                                NitriteConfig nitriteConfig,
                                EventBus<ChangedItem<Document>, ChangeListener> eventBus) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.eventBus = eventBus;
        init();
    }

    public void createIndex(Field field, String indexType, boolean async) {
        try {
            writeLock.lock();
            indexOperations.ensureIndex(field, indexType, async);
        } finally {
            writeLock.unlock();
        }
    }

    public IndexEntry findIndex(Field field) {
        try {
            readLock.lock();
            return indexOperations.findIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public void rebuildIndex(IndexEntry indexEntry, boolean async) {
        try {
            writeLock.lock();
            indexOperations.rebuildIndex(indexEntry, async);
        } finally {
            writeLock.unlock();
        }
    }

    public Collection<IndexEntry> listIndexes() {
        try {
            readLock.lock();
            return indexOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    public boolean hasIndex(Field field) {
        try {
            readLock.lock();
            return indexOperations.hasIndexEntry(field);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isIndexing(Field field) {
        try {
            readLock.lock();
            return indexOperations.isIndexing(field);
        } finally {
            readLock.unlock();
        }
    }

    public void dropIndex(Field field) {
        try {
            writeLock.lock();
            indexOperations.dropIndex(field);
        } finally {
            writeLock.unlock();
        }
    }

    public void dropAllIndices() {
        try {
            writeLock.lock();
            indexOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult insert(Document[] documents) {
        try {
            writeLock.lock();
            return writeOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        try {
            writeLock.lock();
            return writeOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        try {
            writeLock.lock();
            return writeOperations.remove(filter, removeOptions);
        } finally {
            writeLock.unlock();
        }
    }

    public DocumentCursor find() {
        try {
            readLock.lock();
            return readOperations.find();
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter) {
        try {
            readLock.lock();
            return readOperations.find(filter);
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(FindOptions findOptions) {
        try {
            readLock.lock();
            return readOperations.find(findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            return readOperations.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    public Document getById(NitriteId nitriteId) {
        try {
            readLock.lock();
            return readOperations.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    public void dropCollection() {
        try {
            writeLock.lock();
            indexOperations.dropAllIndices();
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
        this.indexOperations = new IndexOperations(nitriteConfig, nitriteMap);
        this.readOperations = new ReadOperations(indexOperations, nitriteMap);
        this.writeOperations = new WriteOperations(indexOperations, readOperations,
            nitriteMap, eventBus);
    }
}