package org.dizitart.no2.store;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class NitriteMVStore implements NitriteStore {
    private StoreConfig storeConfig;
    private NitriteConfig nitriteConfig;
    private NitriteEventBus<EventInfo, StoreEventListener> eventBus;

    public NitriteMVStore(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Set<String> getCollectionNames() {
        return null;
    }

    @Override
    public Map<String, Class<?>> getRepositoryRegistry() {
        return null;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void compact() {

    }

    @Override
    public void commit() {

        alert(StoreEvents.Commit);
    }

    @Override
    public void close() {
        if (storeConfig.autoCompactEnabled) {
            compact();
        }


        alert(StoreEvents.Closed);
    }

    @Override
    public void beforeClose() {
        alert(StoreEvents.Closing);
    }

    @Override
    public IndexCatalog getIndexCatalog() {
        return null;
    }

    @Override
    public <Key, Value> void removeMap(NitriteMap<Key, Value> nitriteMap) {

    }

    @Override
    public <Key, Value> NitriteMap<Key, Value> openMap(String name) {
        return null;
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {

    }

    @Override
    public <Store> Store underlyingStore() {
        return null;
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
        eventBus = new StoreEventBus();
        alert(StoreEvents.Opened);
    }

    private void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, nitriteConfig);
        eventBus.post(event);
    }

    private static class StoreEventBus extends NitriteEventBus<EventInfo, StoreEventListener> {
        @Override
        public void post(EventInfo storeEvent) {
            for (final StoreEventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(storeEvent));
            }
        }
    }
}
