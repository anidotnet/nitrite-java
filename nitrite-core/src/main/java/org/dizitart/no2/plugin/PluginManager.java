package org.dizitart.no2.plugin;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.PE_LOAD_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class PluginManager {
    private Map<Class<?>, Set<Indexer>> indexerMap;
    private Set<Indexer> indexers;
    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;
    private NitriteConfig nitriteConfig;
    private StoreConfig storeConfig;

    public PluginManager(NitriteConfig nitriteConfig) {
        indexerMap = new HashMap<>();
        indexers = new HashSet<>();
        this.nitriteConfig = nitriteConfig;
    }

    public void load(NitritePlugin... plugins) {
        populatePlugins(plugins);
        initializePlugins(plugins);
    }

    public void load(Class<? extends NitritePlugin>... plugins) {
        if (plugins != null) {
            for (Class<? extends NitritePlugin> plugin : plugins) {
                try {
                    Constructor<? extends NitritePlugin> pluginConstructor = plugin.getDeclaredConstructor();
                    NitritePlugin nitritePlugin = pluginConstructor.newInstance();
                    load(nitritePlugin);
                } catch (Throwable t) {
                    log.error("Error while loading plugin " + plugin.getName(), t);
                    throw new PluginException(errorMessage("failed to load plugin " + plugin.getName(),
                        PE_LOAD_FAILED));
                }
            }
        }
    }

    public Set<Indexer> getIndexers() {
        return indexers;
    }

    public NitriteMapper getNitriteMapper() {
        return nitriteMapper;
    }

    public NitriteStore getNitriteStore() {
        return nitriteStore;
    }

    public StoreConfig getStoreConfig() {
        return storeConfig;
    }

    private void initializePlugins(NitritePlugin[] plugins) {
        for (NitritePlugin plugin : plugins) {
            plugin.initialize(nitriteConfig);
        }
    }

    private void populatePlugins(NitritePlugin[] plugins) {
        if (plugins != null) {
            for (NitritePlugin plugin : plugins) {
                loadIfIndexer(plugin);
                loadIfNitriteMapper(plugin);
                loadIfNitriteStore(plugin);
            }
        }
    }

    private void loadIfNitriteStore(NitritePlugin plugin) {
        if (plugin instanceof NitriteStore) {
            this.nitriteStore = (NitriteStore) plugin;
        } else if (plugin instanceof StoreConfig) {
            this.storeConfig = (StoreConfig) plugin;
        }
    }

    private void loadIfNitriteMapper(NitritePlugin plugin) {
        if (plugin instanceof NitriteMapper) {
            this.nitriteMapper = (NitriteMapper) plugin;
        }
    }

    private void loadIfIndexer(NitritePlugin plugin) {
        if (plugin instanceof Indexer) {
            this.indexers.add((Indexer) plugin);
            Set<Indexer> indexerSet = this.indexerMap.get(plugin.getClass());
            if (indexerSet == null) {
                indexerSet = new HashSet<>();
            }
            indexerSet.add((Indexer) plugin);

            indexerMap.put(plugin.getClass(), indexerSet);
        }
    }
}
