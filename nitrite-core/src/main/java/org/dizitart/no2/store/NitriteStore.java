package org.dizitart.no2.store;

import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * Represents a persistent storage for Nitrite database.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public interface NitriteStore extends NitritePlugin, AutoCloseable {

    void openOrCreate(String username, String password, StoreConfig storeConfig);

    /**
     * Checks whether this store is closed for further modification.
     *
     * @return `true` if closed; `false` otherwise.
     */
    boolean isClosed();

    /**
     * Gets the set of all {@link org.dizitart.no2.collection.NitriteCollection} names in store.
     *
     * @return the set of names.
     */
    Set<String> getCollectionNames();

    /**
     * Gets the set of all {@link org.dizitart.no2.collection.objects.ObjectRepository} details in store.
     *
     * @return the details of all {@link org.dizitart.no2.collection.objects.ObjectRepository}.
     */
    Map<String, Class<?>> getRepositoryRegistry();

    /**
     * Checks whether there are any unsaved changes.
     *
     * @return `true` if there are any changes; `false` otherwise.
     */
    boolean hasUnsavedChanges();

    /**
     * Checks whether the store is opened in readonly mode.
     *
     * @return `true` if the store is opened in readonly mode.; `false` otherwise.
     */
    boolean isReadOnly();

    /**
     * Compacts the store by moving all chunks next to each other.
     */
    void compact();

    /**
     * Commits the changes. For persistent stores, it also writes
     * changes to disk. It does nothing if there are no unsaved changes.
     */
    void commit();

    /**
     * Closes the file and the store. Unsaved changes are written to disk first.
     */
    void close();

    /**
     * This method runs before {@link #close()}, to run cleanup routines.
     */
    void beforeClose();

    /**
     * Gets the {@link IndexCatalog} instances from the store.
     *
     * @return the IndexCatalog instance.
     */
    IndexCatalog getIndexCatalog();

    /**
     * Opens a {@link NitriteMap} with the default settings. The map is
     * automatically create if it does not yet exist. If a map with this
     * name is already open, this map is returned.
     *
     * @param <Key>   the key type
     * @param <Value> the value type
     * @param mapName the map name
     * @return the map.
     */
    <Key, Value> NitriteMap<Key, Value> openMap(String mapName);

    /**
     * Removes a map from the store.
     *
     * @param mapName the map name to remove.
     */
    void removeMap(String mapName);

    /**
     * Opens a {@link NitriteRTree} with the default settings. The RTree is
     * automatically create if it does not yet exist. If a RTree with this
     * name is already open, this RTree is returned.
     *
     * @param <Key>   the key type
     * @param <Value> the value type
     * @param rTreeName the RTree name
     * @return the map.
     */
    <Key, Value> NitriteRTree<Key, Value> openRTree(String rTreeName);

    /**
     * Removes a RTree from the store.
     *
     * @param rTreeName the RTree name to remove.
     */
    void removeRTree(String rTreeName);

    /**
     * Adds a {@link StoreEventListener} to listen to all store events.
     *
     * @param listener the listener instances.
     * */
    void addStoreEventListener(StoreEventListener listener);

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
}
