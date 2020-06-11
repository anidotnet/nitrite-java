package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;

import static org.dizitart.no2.common.util.ObjectUtils.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    private final Map<String, ObjectRepository<?>> repositoryMap;
    private final Map<String, NitriteCollection> collectionMap;
    private final NitriteConfig nitriteConfig;
    private NitriteStore store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.collectionMap = new HashMap<>();
        this.repositoryMap = new HashMap<>();
        this.initialize(null, null);
    }

    NitriteDatabase(String username, String password, NitriteConfig config) {
        validateUserCredentials(username, password);
        this.nitriteConfig = config;
        this.collectionMap = new HashMap<>();
        this.repositoryMap = new HashMap<>();
        this.initialize(username, password);
    }

    @Override
    public NitriteCollection getCollection(String name) {
        validateCollectionName(name);
        checkOpened();
        if (collectionMap.containsKey(name)) {
            NitriteCollection collection = collectionMap.get(name);
            if (collection.isDropped()) {
                collectionMap.remove(name);
                return createAndGetCollection(name);
            }
            return collectionMap.get(name);
        } else {
            return createAndGetCollection(name);
        }
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        checkOpened();
        String name = findRepositoryName(type);
        return getRepositoryByName(name, type);
    }

    @Override
    public <T> ObjectRepository<T> getRepository(String key, Class<T> type) {
        checkOpened();
        String name = findRepositoryName(type, key);
        return getRepositoryByName(name, type);
    }

    @Override
    public Set<String> listCollectionNames() {
        checkOpened();
        return new LinkedHashSet<>(store.getCollectionNames());
    }

    @Override
    public Set<String> listRepositories() {
        checkOpened();
        Set<String> resultSet = new LinkedHashSet<>();
        Set<String> repositories = store.getRepositoryRegistry().keySet();
        for (String name : repositories) {
            if (!isKeyedRepository(name)) {
                resultSet.add(name);
            }
        }
        return resultSet;
    }

    @Override
    public Map<String, Set<String>> listKeyedRepository() {
        checkOpened();
        Map<String, Set<String>> resultMap = new HashMap<>();
        Set<String> repositories = store.getRepositoryRegistry().keySet();
        for (String name : repositories) {
            if (isKeyedRepository(name)) {
                String key = getKeyName(name);
                String type = getKeyedRepositoryType(name);

                Set<String> types;
                if (resultMap.containsKey(key)) {
                    types = resultMap.get(key);
                } else {
                    types = new HashSet<>();
                }
                types.add(type);
                resultMap.put(key, types);
            }
        }
        return resultMap;
    }

    @Override
    public boolean hasUnsavedChanges() {
        checkOpened();
        return store != null && store.hasUnsavedChanges();
    }

    @Override
    public boolean isClosed() {
        return store == null || store.isClosed();
    }

    @Override
    public NitriteStore getStore() {
        return store;
    }

    @Override
    public NitriteConfig getConfig() {
        return nitriteConfig;
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
            log.info("Nitrite database has been closed successfully.");
        } catch (Throwable error) {
            if (!nitriteConfig.getStoreConfig().isReadOnly()) {
                throw new NitriteIOException("error while shutting down nitrite", error);
            }
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

    private NitriteCollection createAndGetCollection(String name) {
        NitriteCollection collection = CollectionFactory.getCollection(name, nitriteConfig);
        collectionMap.put(name, collection);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectRepository<T> getRepositoryByName(String name, Class<T> type) {
        checkOpened();
        if (repositoryMap.containsKey(name)) {
            ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(name);
            if (repository.isDropped()) {
                repositoryMap.remove(name);
                return createAndGetRepository(name, type);
            } else {
                return repository;
            }
        } else {
            return createAndGetRepository(name, type);
        }
    }

    private <T> ObjectRepository<T> createAndGetRepository(String name, Class<T> type) {
        ObjectRepository<T> repository = RepositoryFactory.getRepository(type, name, nitriteConfig);
        repositoryMap.put(name, repository);
        return repository;
    }

    private void validateUserCredentials(String username, String password) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new SecurityException("username cannot be empty");
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new SecurityException("password cannot be empty");
        }
    }

    private void initialize(String username, String password) {
        this.nitriteConfig.initialized();
        this.store = nitriteConfig.getNitriteStore();
        this.store.openOrCreate(username, password, nitriteConfig.getStoreConfig());
    }

    private void closeCollections() {
        checkOpened();
        Set<String> collections = store.getCollectionNames();
        if (collections != null) {
            for (String name : collections) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && collection.isOpen()) {
                    collection.close();
                }
            }
            collections.clear();
            collectionMap.clear();
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
            repositoryMap.clear();
        }
    }
}
