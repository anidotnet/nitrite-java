package org.dizitart.no2.store;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;
import org.h2.mvstore.MVStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.ErrorCodes.VE_INVALID_STORE_CONFIG;
import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.common.util.ObjectUtils.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.exceptions.ErrorMessage.NITRITE_STORE_IS_CLOSED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class MVStoreOperations {
    private MVStore mvStore;
    private MVStoreConfig mvStoreConfig;
    private NitriteConfig nitriteConfig;
    private NitriteEventBus<EventInfo, StoreEventListener> eventBus;
    private Set<String> collectionRegistry;
    private Map<String, Class<?>> repositoryRegistry;

    MVStoreOperations(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
        this.collectionRegistry = new HashSet<>();
        this.repositoryRegistry = new HashMap<>();
        this.eventBus = new StoreEventBus();
    }

    private void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, nitriteConfig);
        eventBus.post(event);
    }

    void openOrCreate(String username, String password, StoreConfig storeConfig) {
        validateStoreConfig(storeConfig);
        this.mvStoreConfig = (MVStoreConfig) storeConfig;
        this.mvStore = MVStoreUtils.openOrCreate(username, password, mvStoreConfig);
        populateCollections();
    }

    private void validateStoreConfig(StoreConfig storeConfig) {
        if (!(storeConfig instanceof MVStoreConfig)) {
            throw new ValidationException(errorMessage("store config is not valid mvstore config",
                VE_INVALID_STORE_CONFIG));
        }
    }

    boolean isClosed() {
        return mvStore.isClosed();
    }

    Set<String> getCollectionNames() {
        return collectionRegistry;
    }

    private boolean isValidCollectionName(String name) {
        if (isNullOrEmpty(name)) return false;
        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) return false;
        }
        return true;
    }

    //TODO: add collection Names when new one is opened
    private void populateCollections() {
        Set<String> mapNames = mvStore.getMapNames();
        for (String name : mapNames) {
            if (isValidCollectionName(name) && !isRepository(name)) {
                collectionRegistry.add(name);
            }
        }
    }

    // TODO: add repository when new one is created
    private void populateRepositories() {
        for (String name : mvStore.getMapNames()) {
            if (isValidCollectionName(name) && isRepository(name)) {
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
        }
    }

    Map<String, Class<?>> getRepositoryRegistry() {
        return repositoryRegistry;
    }

    boolean hasUnsavedChanges() {
        return mvStore != null && mvStore.hasUnsavedChanges();
    }

    boolean isReadOnly() {
        return mvStore.isReadOnly();
    }

    void compact() {

    }

    private void checkOpened() {
        if (mvStore == null || mvStore.isClosed()) {
            throw new NitriteIOException(NITRITE_STORE_IS_CLOSED);
        }
    }

    void commit() {
        mvStore.commit();
        alert(StoreEvents.Commit);
    }

    void close() {
        if (mvStoreConfig.isAutoCompact()) {
            compact();
        }
        alert(StoreEvents.Closed);
    }

    void beforeClose() {
        alert(StoreEvents.Closing);
    }
}
