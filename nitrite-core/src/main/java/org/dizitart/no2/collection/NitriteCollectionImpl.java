package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.operation.CollectionOperation;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee.
 */
class NitriteCollectionImpl implements NitriteCollection {
    private NitriteStore nitriteStore;
    private CollectionOperation collectionOperation;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private NitriteConfig nitriteConfig;
    private final String collectionName;
    private volatile boolean isDropped;

    NitriteCollectionImpl(String name, NitriteConfig nitriteConfig) {
        this.collectionName = name;
        this.nitriteConfig = nitriteConfig;
        initCollection();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    private void initCollection() {
        nitriteStore = nitriteConfig.getNitriteStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperation = new CollectionOperation(nitriteStore, nitriteConfig, eventBus);
    }

    private static class CollectionEventBus extends NitriteEventBus<ChangedItem<Document>, ChangeListener> {
        @Override
        public void post(ChangedItem<Document> changedItem) {
            for (final ChangeListener listener : getListeners()) {
                String threadName = Thread.currentThread().getName();
                changedItem.setOriginatingThread(threadName);

                getEventExecutor().submit(() -> listener.onChange(changedItem));
            }
        }
    }
}
