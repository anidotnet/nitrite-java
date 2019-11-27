package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.RepositoryFactory;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.util.ObjectUtils.*;
import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_CLOSED_NON_W_CHANNEL;
import static org.dizitart.no2.exceptions.ErrorMessage.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    private final NitriteConfig nitriteConfig;
    private NitriteStore store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.initialize(null, null);
    }

    NitriteDatabase(String username, String password, NitriteConfig config) {
        validateUserCredentials(username, password);
        this.nitriteConfig = config;
        this.initialize(username, password);
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
    public void addEventListener(StoreEventListener listener) {
        store.addStoreEventListener(listener);
    }

    @Override
    public synchronized void close() {
        checkOpened();
        try {
            store.beforeClose();
            if (hasUnsavedChanges()) {
                log.debug("Unsaved changes detected, committing the changes.");
                commit();
            }

            closeCollections();
            store.close();
        } catch (Throwable error) {
            if (!nitriteConfig.getStoreConfig().isReadOnly()) {
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
        if (store != null && !nitriteConfig.getStoreConfig().isReadOnly()) {
            store.commit();
            log.debug("Unsaved changes committed successfully.");
        }
    }

    private void validateUserCredentials(String username, String password) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new SecurityException(USER_ID_IS_EMPTY);
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new SecurityException(PASSWORD_IS_EMPTY);
        }
    }

    private void initialize(String username, String password) {
        nitriteConfig.initialized();
        this.store = nitriteConfig.getNitriteStore();
        this.store.openOrCreate(username, password, nitriteConfig.getStoreConfig());
    }

    private void closeCollections() {
        Set<String> collections = store.getCollectionNames();
        if (collections != null) {
            for (String name : collections) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && collection.isOpen()) {
                    collection.close();
                }
            }
            collections.clear();
        }

        Map<String, Class<?>> repositories = store.getRepositoryRegistry();
        if (repositories != null) {
            for (String name : repositories.keySet()) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && collection.isOpen()) {
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
            int timeout = nitriteConfig.getPoolShutdownTimeout();
            ExecutorServiceManager.shutdownExecutors(timeout);
        } catch (Throwable t) {
            log.error("Error while shutting down database gracefully", t);
        } finally {
            store = null;
        }
    }
}
