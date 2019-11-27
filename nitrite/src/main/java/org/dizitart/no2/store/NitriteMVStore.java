package org.dizitart.no2.store;


import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.events.StoreEventListener;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class NitriteMVStore implements NitriteStore {
    private MVStoreOperations storeOperations;

    @Override
    public void openOrCreate(String username, String password, StoreConfig storeConfig) {
        this.storeOperations.openOrCreate(username, password, storeConfig);
    }

    @Override
    public boolean isClosed() {
        return this.storeOperations.isClosed();
    }

    @Override
    public Set<String> getCollectionNames() {
        return this.storeOperations.getCollectionNames();
    }

    @Override
    public Map<String, Class<?>> getRepositoryRegistry() {
        return this.storeOperations.getRepositoryRegistry();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return this.storeOperations.hasUnsavedChanges();
    }

    @Override
    public boolean isReadOnly() {
        return this.storeOperations.isReadOnly();
    }

    @Override
    public void compact() {
        this.storeOperations.compact();
    }

    @Override
    public void commit() {
        this.storeOperations.commit();
    }

    @Override
    public void close() {
        this.storeOperations.close();
    }

    @Override
    public void beforeClose() {
        this.storeOperations.beforeClose();
    }

    @Override
    public IndexCatalog getIndexCatalog() {
        return null;
    }

    @Override
    public <Key, Value> NitriteMap<Key, Value> openMap(String name) {
        return null;
    }

    @Override
    public void removeMap(String mapName) {

    }

    @Override
    public <Key, Value> NitriteRTree<Key, Value> openRTree(String rTreeName) {
        return null;
    }

    @Override
    public void removeRTree(String rTreeName) {

    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {

    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.storeOperations = new MVStoreOperations(nitriteConfig);
    }

}
