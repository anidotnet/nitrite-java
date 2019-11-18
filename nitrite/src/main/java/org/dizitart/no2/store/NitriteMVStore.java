package org.dizitart.no2.store;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class NitriteMVStore implements NitriteStore {
    private StoreConfig storeConfig;
    private NitriteConfig nitriteConfig;

    public NitriteMVStore(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public NitriteCollection getCollection(String name, NitriteConfig nitriteConfig) {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Set<String> getCollectionNames() {
        return null;
    }

    @Override
    public <T> ObjectRepository<T> getRepository(Class<T> type) {
        return null;
    }

    @Override
    public <T> ObjectRepository<T> getRepository(String key, Class<T> type) {
        return null;
    }

    @Override
    public Map<String, Class<?>> getRepositoryRegistry() {
        return null;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void compact() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void close() {
        if (storeConfig.autoCompactEnabled) {
            compact();
        }
        //TODO: close store
    }

    @Override
    public void beforeClose() {

    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
    }
}
