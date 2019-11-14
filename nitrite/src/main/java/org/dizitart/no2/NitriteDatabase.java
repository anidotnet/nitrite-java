package org.dizitart.no2;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteStore;

import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorMessage.NITRITE_STORE_IS_CLOSED;

/**
 * @author Anindya Chatterjee.
 */
class NitriteDatabase implements Nitrite {
    private final NitriteConfig nitriteConfig;
    private NitriteStore store;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.initialize();
    }

    @Override
    public NitriteCollection getCollection(String name) {
        validateCollectionName(name);
        checkOpened();
        return store.getCollection(name);
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
        String name = findRepositoryName(key, type);
        return getRepositoryByName(name, type);
    }

    @Override
    public Set<String> listCollectionNames() {
        return null;
    }

    @Override
    public Set<String> listRepositories() {
        return null;
    }

    @Override
    public Map<String, String> listKeyedRepository() {
        return null;
    }

    @Override
    public boolean hasCollection(String name) {
        return false;
    }

    @Override
    public <T> boolean hasRepository(Class<T> type) {
        return false;
    }

    @Override
    public <T> boolean hasRepository(String key, Class<T> type) {
        return false;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public void compact() {

    }

    @Override
    public void commit() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    private void initialize() {
        store = nitriteConfig.getNitriteStore();
    }

    private void checkOpened() {
        if (store == null || store.isClosed()) {
            throw new NitriteIOException(NITRITE_STORE_IS_CLOSED);
        }
    }

    private <T> ObjectRepository<T> getRepositoryByName(String name, Class<T> type) {

    }
}
