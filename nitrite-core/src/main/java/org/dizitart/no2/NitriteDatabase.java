package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.RepositoryFactory;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.common.event.DatabaseEvent;
import org.dizitart.no2.common.event.DatabaseEventListener;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteStore;

import java.nio.channels.NonWritableChannelException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.util.ObjectUtils.*;
import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_CLOSED_NON_W_CHANNEL;
import static org.dizitart.no2.exceptions.ErrorMessage.NITRITE_STORE_IS_CLOSED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    private final NitriteConfig nitriteConfig;
    private NitriteStore store;
    private NitriteEventBus<DatabaseEvent, DatabaseEventListener> eventBus;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.initialize();
    }

    @Override
    public NitriteCollection getCollection(String name) {
        validateCollectionName(name);
        checkOpened();
        return CollectionFactory.getCollection(name, nitriteConfig);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        checkOpened();
        return RepositoryFactory.getRepository(type);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(String key, Class<T> type) {
        checkOpened();
        return RepositoryFactory.getRepository(key, type);
    }

    @Override
    public Set<String> listCollectionNames() {
        return new LinkedHashSet<>(store.getCollectionNames());
    }

    @Override
    public Set<String> listRepositories() {
        Set<String> resultSet = new LinkedHashSet<>();
        Set<String> repository = store.getRepositoryRegistry().keySet();
        for (String name : repository) {
            if (!isKeyedRepository(name)) {
                resultSet.add(name);
            }
        }
        return resultSet;
    }

    @Override
    public Map<String, String> listKeyedRepository() {
        Map<String, String> resultMap = new HashMap<>();
        Set<String> repository = store.getRepositoryRegistry().keySet();
        for (String name : repository) {
            if (isKeyedRepository(name)) {
                String key = getKeyName(name);
                String type = getKeyedRepositoryType(name);
                resultMap.put(key, type);
            }
        }
        return resultMap;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return store != null && store.hasUnsavedChanges();
    }

    @Override
    public boolean isClosed() {
        return store == null || store.isClosed();
    }

    @Override
    public void addEventListener(DatabaseEventListener listener) {
        eventBus.register(listener);
    }

    @Override
    public synchronized void close() {
        checkOpened();
        try {
            notify(DatabaseEvent.EventType.Closing);
            store.beforeClose();
            if (hasUnsavedChanges()) {
                log.debug("Unsaved changes detected, committing the changes.");
                commit();
            }

            closeCollections();
            store.close();
        } catch (Throwable error) {
            if (!nitriteConfig.isReadOnly()) {
                throw new NitriteIOException(errorMessage("error while shutting down nitrite",
                    NIOE_CLOSED_NON_W_CHANNEL), error);
            }
        } finally {
            shutdown();
            log.info("Nitrite database has been closed successfully.");
        }
    }

    @Override
    public void commit() {
        checkOpened();
        if (store != null && !nitriteConfig.isReadOnly()) {
            store.commit();
            notify(DatabaseEvent.EventType.Commit);
            log.debug("Unsaved changes committed successfully.");
        }
    }

    private void initialize() {
        store = nitriteConfig.getNitriteStore();
        eventBus = new DatabaseEventBus();
        notify(DatabaseEvent.EventType.Opened);
    }

    private void notify(DatabaseEvent.EventType eventType) {
        DatabaseEvent event = new DatabaseEvent(eventType, nitriteConfig);
        eventBus.post(event);
    }

    private void closeCollections() {
        Set<String> collections = store.getCollectionNames();
        if (collections != null) {
            for (String name : collections) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            collections.clear();
        }

        Map<String, Class<?>> repositories = store.getRepositoryRegistry();
        if (repositories != null) {
            for (String name : repositories.keySet()) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            repositories.clear();
        }
    }

    private void checkOpened() {
        if (store == null || store.isClosed()) {
            throw new NitriteIOException(NITRITE_STORE_IS_CLOSED);
        }
    }

    private void shutdown() {
        try {
            notify(DatabaseEvent.EventType.Closed);
            int timeout = nitriteConfig.getPoolShutdownTimeout();
            ExecutorServiceManager.shutdownExecutors(timeout);
        } catch (Throwable t) {
            log.error("Error while shutting down database gracefully", t);
        } finally {
            store = null;
        }
    }

    private static class DatabaseEventBus extends NitriteEventBus<DatabaseEvent, DatabaseEventListener> {
        @Override
        public void post(DatabaseEvent databaseEvent) {
            for (final DatabaseEventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(databaseEvent));
            }
        }
    }
}
