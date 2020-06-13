package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.ObjectUtils.getEntityName;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactory {
    private final Map<String, ObjectRepository<?>> repositoryMap;
    private final CollectionFactory collectionFactory;
    private final ReentrantLock lock;

    public RepositoryFactory(CollectionFactory collectionFactory) {
        this.collectionFactory = collectionFactory;
        this.repositoryMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type) {
        return getRepository(nitriteConfig, type, null);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type, String key) {
        if (type == null) {
            throw new ValidationException("type cannot be null");
        }

        if (nitriteConfig == null) {
            throw new ValidationException("nitriteConfig cannot be null");
        }

        String collectionName = findRepositoryName(key, type);

        try {
            lock.lock();
            if (repositoryMap.containsKey(collectionName)) {
                ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
                if (repository.isDropped()) {
                    repositoryMap.remove(collectionName);
                    return createRepository(nitriteConfig, type, collectionName, key);
                } else {
                    return repository;
                }
            } else {
                return createRepository(nitriteConfig, type, collectionName, key);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        try {
            lock.lock();
            for (ObjectRepository<?> repository : repositoryMap.values()) {
                repository.close();
            }
            repositoryMap.clear();
        } finally {
            lock.unlock();
        }
    }

    private <T> ObjectRepository<T> createRepository(NitriteConfig nitriteConfig, Class<T> type,
                                                     String collectionName, String key) {
        NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
        NitriteStore store = nitriteConfig.getNitriteStore();
        if (nitriteMapper.isValueType(type)) {
            throw new ValidationException("a value type cannot be used to create repository");
        }

        if (store.getCollectionNames().contains(collectionName)) {
            throw new ValidationException("a collection with same entity name already exists");
        }

        NitriteCollection nitriteCollection = collectionFactory.getCollection(collectionName,
            nitriteConfig, false);
        ObjectRepository<T> repository = new DefaultObjectRepository<>(type, nitriteCollection, nitriteConfig);
        repositoryMap.put(collectionName, repository);

        writeCatalogue(store, collectionName, key);
        return repository;
    }

    private void writeCatalogue(NitriteStore store, String name, String key) {
        NitriteMap<String, Document> catalogueMap = store.openMap(COLLECTION_CATALOGUE);
        Document document = StringUtils.isNullOrEmpty(key) ? catalogueMap.get(TAG_REPOSITORIES)
            : catalogueMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) document = Document.createDocument();

        document.put(name, true);
        if (StringUtils.isNullOrEmpty(key)) {
            catalogueMap.put(TAG_REPOSITORIES, document);
        } else {
            catalogueMap.put(TAG_KEYED_REPOSITORIES, document);
        }
    }

    private <T> String findRepositoryName(String key, Class<T> type) {
        if (StringUtils.isNullOrEmpty(key)) {
            return getEntityName(type);
        } else {
            return getEntityName(type) + KEY_OBJ_SEPARATOR + key;
        }
    }
}
