/*
 *  Copyright 2017-2019 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.events.StoreEventListener;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * = Nitrite
 *
 * An in-memory, single-file based embedded nosql persistent document store. The store
 * can contains multiple named document collections.
 *
 * It supports following features:
 *
 * include::/src/docs/asciidoc/features.adoc[]
 *
 * [icon="{@docRoot}/note.png"]
 * [NOTE]
 * ====
 *  - It does not support ACID transactions.
 * ====
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface Nitrite extends Closeable {

    /**
     * Opens or creates a new database. If it is an in-memory store, then it
     * will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link NitriteIOException}.
     *
     * It also adds a JVM shutdown hook to the database instance. If JVM exists
     * before closing the database properly by calling {@link Nitrite#close()},
     * then the shutdown hook will try to close the database as soon as possible
     * by discarding any unsaved changes to avoid database corruption.
     *
     * --
     * @param nitriteConfig database configuration.
     * @return the nitrite database instance.
     * @throws NitriteIOException if unable to create a new in-memory database.
     * @throws NitriteIOException if the database is corrupt and recovery fails.
     * @throws IllegalArgumentException if the directory does not exist.
     * @see NitriteConfig
     */
    static Nitrite openOrCreate(NitriteConfig nitriteConfig) {
        return new NitriteDatabase(nitriteConfig);
    }

    /**
     * Opens or creates a new database. If it is an in-memory store, then it
     * will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link NitriteIOException}.
     *
     * It also adds a JVM shutdown hook to the database instance. If JVM exists
     * before closing the database properly by calling {@link Nitrite#close()},
     * then the shutdown hook will try to close the database as soon as possible
     * by discarding any unsaved changes to avoid database corruption.
     *
     * --
     * @param username   the user id
     * @param password the password
     * @return the nitrite database instance.
     * @throws NitriteIOException if unable to create a new in-memory database.
     * @throws NitriteIOException if the database is corrupt and recovery fails.
     * @throws IllegalArgumentException if the directory does not exist.
     */
    static Nitrite openOrCreate(String username, String password, NitriteConfig nitriteConfig) {
        return new NitriteDatabase(username, password, nitriteConfig);
    }

    /**
     * Closes the database. Unsaved changes are written to disk and compacted first
     * for a file based store.
     */
    void close();

    /**
     * Commits the changes. For file based store, it saves the changes
     * to disk if there are any unsaved changes.
     *
     * [icon="{@docRoot}/tip.png"]
     * TIP: No need to call it after every change, if auto-commit is not disabled
     * while opening the db. However, it may still be called to flush all
     * changes to disk.
     *
     */
    void commit();

    /**
     * Opens a named collection from the store. If the collections does not
     * exist it will be created automatically and returned. If a collection
     * is already opened, it is returned as is. Returned collection is thread-safe
     * for concurrent use.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The name cannot contain below reserved strings:
     *
     * - {@link Constants#INTERNAL_NAME_SEPARATOR}
     * - {@link Constants#USER_MAP}
     * - {@link Constants#INDEX_META_PREFIX}
     * - {@link Constants#INDEX_PREFIX}
     * - {@link Constants#OBJECT_STORE_NAME_SEPARATOR}
     *
     * ====
     *
     * @param name the name of the collection
     * @return the collection
     * @see NitriteCollection
     */
    NitriteCollection getCollection(String name);

    /**
     * Opens a type-safe object repository from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: The returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type);

    /**
     * Opens a type-safe object repository with a key identifier from the store. If the repository
     * does not exist it will be created automatically and returned. If a
     * repository is already opened, it is returned as is.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: The returned repository is thread-safe for concurrent use.
     *
     * @param <T>  the type parameter.
     * @param key  the key, which will be appended to the repositories name.
     * @param type the type of the object.
     * @return the repository containing objects of type {@link T}.
     * @see ObjectRepository
     */
    <T> ObjectRepository<T> getRepository(String key, Class<T> type);

    /**
     * Gets the set of all {@link NitriteCollection}s' names saved in the store.
     *
     * @return the set of all collections' names.
     */
    Set<String> listCollectionNames();

    /**
     * Gets the set of all fully qualified class names corresponding
     * to all {@link ObjectRepository}s in the store.
     *
     * @return the set of all registered classes' names.
     */
    Set<String> listRepositories();

    /**
     * Gets the map of all key to the fully qualified class names corresponding
     * to all keyed-{@link ObjectRepository}s in the store.
     *
     * @return the set of all registered classes' names.
     */
    Map<String, Set<String>> listKeyedRepository();

    /**
     * Checks whether the store has any unsaved changes.
     *
     * @return `true` if there are unsaved changes; otherwise `false`.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether the store is closed.
     *
     * @return `true` if closed; otherwise `false`.
     */
    boolean isClosed();

    /**
     * Adds an event listener, which listens to various database events.
     *
     * @param listener the database event listener.
     * @see org.dizitart.no2.store.events.StoreEvents
     * */
    void addEventListener(StoreEventListener listener);

    /**
     * Gets the {@link NitriteConfig} instance to configure the database.
     *
     * @return the {@link NitriteConfig} instance to configure the database.
     * */
    NitriteConfig getConfig();

    /**
     * Checks whether a particular {@link NitriteCollection} exists in the store.
     *
     * @param name the name of the collection.
     * @return `true` if the collection exists; otherwise `false`.
     */
    default boolean hasCollection(String name) {
        return listCollectionNames().contains(name);
    }

    /**
     * Checks whether a particular {@link ObjectRepository} exists in the store.
     *
     * @param <T>  the type parameter
     * @param type the type of the object
     * @return `true` if the repository exists; otherwise `false`.
     */
    default <T> boolean hasRepository(Class<T> type) {
        return listRepositories().contains(type.getName());
    }

    /**
     * Checks whether a particular keyed-{@link ObjectRepository} exists in the store.
     *
     * @param <T>  the type parameter.
     * @param key  the key, which will be appended to the repositories name.
     * @param type the type of the object.
     * @return `true` if the repository exists; otherwise `false`.
     */
    default <T> boolean hasRepository(String key, Class<T> type) {
        return listKeyedRepository().containsKey(key)
            && listKeyedRepository().get(key).contains(type.getName());
    }

    default void validateCollectionName(String name) {
        notNull(name, "name cannot be null");
        notEmpty(name, "name cannot be empty");

        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) {
                throw new ValidationException("name cannot contain " + reservedName);
            }
        }
    }

    default <T> String findRepositoryName(String key, Class<T> type) {
        notNull(key, "key cannot be null");
        notEmpty(key, "key cannot be empty");
        notNull(type, "type cannot be null");
        return type.getName() + KEY_OBJ_SEPARATOR + key;
    }

    default <T> String findRepositoryName(Class<T> type) {
        notNull(type, "type cannot be null");
        return type.getName();
    }
}
