package org.dizitart.no2.store;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.plugin.NitritePlugin;

import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorCodes.VE_OBJ_STORE_NULL_TYPE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteStore extends NitritePlugin, AutoCloseable {
    NitriteCollection getCollection(String name, NitriteConfig nitriteConfig);

    boolean isClosed();

    // populate existing collection names when loading
    Set<String> getCollectionNames();

    <T> ObjectRepository<T> getRepository(Class<T> type);

    <T> ObjectRepository<T> getRepository(String key, Class<T> type);

    Map<String, Class<?>> getRepositoryRegistry();

    boolean hasUnsavedChanges();

    boolean isReadOnly();

    void compact();

    // TODO: create a chain to trigger external store commit also, like lucene
    // may be send a message across message bus and listen to it
    void commit();

    void close();

    void beforeClose();

    default <T> String findRepositoryName(String key, Class<T> type) {
        notNull(key, errorMessage("key cannot be null", VE_OBJ_STORE_NULL_KEY));
        notEmpty(key, errorMessage("key cannot be empty", VE_OBJ_STORE_EMPTY_KEY));
        notNull(type, errorMessage("type cannot be null", VE_OBJ_STORE_NULL_TYPE));
        return type.getName() + KEY_OBJ_SEPARATOR + key;
    }

    default <T> String findRepositoryName(Class<T> type) {
        notNull(type, errorMessage("type cannot be null", VE_OBJ_STORE_NULL_TYPE));
        return type.getName();
    }

    long getCollectionSize(String collectionName);

    void dropCollection(String collectionName);

    void closeCollection(String collectionName);
}
