package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.store.NitriteStore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anindya Chatterjee
 */
public class CollectionOperation {
    private NitriteConfig nitriteConfig;
    private NitriteStore nitriteStore;
    private IndexTemplate indexTemplate;
    private ReadWriteOperation readWriteOperation;
    private QueryTemplate queryTemplate;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private Lock readLock;
    private Lock writeLock;

    public CollectionOperation(NitriteStore nitriteStore,
                        NitriteConfig nitriteConfig,
                        EventBus<ChangedItem<Document>, ChangeListener> eventBus) {
        this.nitriteStore = nitriteStore;
        this.nitriteConfig = nitriteConfig;
        this.eventBus = eventBus;
        init();
    }

    private void init() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.indexTemplate = new IndexTemplate(nitriteConfig, nitriteStore);
        this.queryTemplate = new QueryTemplate(indexTemplate, nitriteConfig, nitriteStore);
        this.readWriteOperation = new ReadWriteOperation(indexTemplate, queryTemplate, nitriteConfig, nitriteStore, eventBus);
    }
}
