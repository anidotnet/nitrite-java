package org.dizitart.no2.store;


import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.common.util.ObjectUtils.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteMVStore implements NitriteStore {
    private final NitriteEventBus<EventInfo, StoreEventListener> eventBus;
    private final Set<String> collectionRegistry;
    private final Map<String, Class<?>> repositoryRegistry;
    private MVStore mvStore;
    private MVStoreConfig mvStoreConfig;
    private NitriteConfig nitriteConfig;

    public NitriteMVStore() {
        this.collectionRegistry = new HashSet<>();
        this.repositoryRegistry = new HashMap<>();
        this.eventBus = new StoreEventBus();
    }

    @Override
    public void openOrCreate(String username, String password, StoreConfig storeConfig) {
        validateStoreConfig(storeConfig);
        this.mvStoreConfig = (MVStoreConfig) storeConfig;
        this.mvStore = MVStoreUtils.openOrCreate(username, password, mvStoreConfig);
        populateCollections();
        populateRepositories();
        initEventBus();
        alert(StoreEvents.Opened);
    }

    @Override
    public boolean isClosed() {
        return mvStore.isClosed();
    }

    @Override
    public Set<String> getCollectionNames() {
        return collectionRegistry;
    }

    @Override
    public Map<String, Class<?>> getRepositoryRegistry() {
        return repositoryRegistry;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return mvStore != null && mvStore.hasUnsavedChanges();
    }

    @Override
    public boolean isReadOnly() {
        return mvStore.isReadOnly();
    }

    @Override
    public void commit() {
        mvStore.commit();
        alert(StoreEvents.Commit);
    }

    @Override
    public void close() {
        if (mvStoreConfig.isAutoCompact()) {
            compact();
        }
        mvStore.close();
        alert(StoreEvents.Closed);
    }

    @Override
    public void beforeClose() {
        alert(StoreEvents.Closing);
    }

    @Override
    public IndexCatalog getIndexCatalog() {
        return new MVStoreIndexCatalog(this);
    }

    @Override
    public <Key, Value> NitriteMap<Key, Value> openMap(String name) {
        if (isValidCollectionName(name)) {
            if (!isRepository(name)) {
                collectionRegistry.add(name);
            } else {
                addRepositoryName(name);
            }
        }

        MVMap<Key, Value> mvMap = mvStore.openMap(name);
        return new NitriteMVMap<>(mvMap, this);
    }

    @Override
    public void removeMap(String name) {
        if (isValidCollectionName(name)) {
            if (!isRepository(name)) {
                collectionRegistry.remove(name);
            } else {
                repositoryRegistry.remove(name);
            }
        }

        MVMap<?, ?> mvMap = mvStore.openMap(name);
        mvStore.removeMap(mvMap);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <Key extends BoundingBox, Value> NitriteRTree<Key, Value> openRTree(String name) {
        MVRTreeMap<Value> map = mvStore.openMap(name, new MVRTreeMap.Builder<>());
        return new NitriteMVRTreeMap(map);
    }

    @Override
    public void removeRTree(String mapName) {
        this.removeMap(mapName);
    }

    @Override
    public void subscribe(StoreEventListener listener) {
        eventBus.register(listener);
    }

    @Override
    public void unsubscribe(StoreEventListener listener) {
        eventBus.deregister(listener);
    }

    @Override
    public StoreInfo getStoreInfo() {
        return MVStoreUtils.getStoreInfo(mvStore);
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
    }

    public void compact() {
        mvStore.compactMoveChunks();
    }

    private void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, nitriteConfig);
        eventBus.post(event);
    }

    private void validateStoreConfig(StoreConfig storeConfig) {
        if (!(storeConfig instanceof MVStoreConfig)) {
            throw new ValidationException("store config is not valid mv store config");
        }
    }

    private boolean isValidCollectionName(String name) {
        if (isNullOrEmpty(name)) return false;
        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) return false;
        }
        return true;
    }

    private void populateCollections() {
        Set<String> mapNames = mvStore.getMapNames();
        for (String name : mapNames) {
            if (isValidCollectionName(name) && !isRepository(name)) {
                collectionRegistry.add(name);
            }
        }
    }

    private void populateRepositories() {
        for (String name : mvStore.getMapNames()) {
            if (isValidCollectionName(name) && isRepository(name)) {
                addRepositoryName(name);
            }
        }
    }

    private void addRepositoryName(String name) {
        try {
            if (isKeyedRepository(name)) {
                String typeName = getKeyedRepositoryType(name);
                Class<?> type = Class.forName(typeName);
                repositoryRegistry.put(name, type);
            } else {
                Class<?> type = Class.forName(name);
                repositoryRegistry.put(name, type);
            }
        } catch (ClassNotFoundException e) {
            log.error("Could not find the class " + name);
        }
    }

    private void initEventBus() {
        if (mvStoreConfig.getEventListeners() != null) {
            for (StoreEventListener eventListener : mvStoreConfig.getEventListeners()) {
                eventBus.register(eventListener);
            }
        }
    }
}
