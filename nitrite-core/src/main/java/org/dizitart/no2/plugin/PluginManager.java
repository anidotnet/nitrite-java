package org.dizitart.no2.plugin;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.indexer.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.util.*;

/**
 * @author Anindya Chatterjee.
 */
public class PluginManager {
    private Map<Class<?>, Set<Indexer>> indexerMap;
    private Set<Indexer> indexers;
    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;
    private NitriteConfig nitriteConfig;

    public PluginManager(NitriteConfig nitriteConfig) {
        indexerMap = new HashMap<>();
        indexers = new HashSet<>();
        this.nitriteConfig = nitriteConfig;
    }

    public void load(NitritePlugin[] plugins) {
        populatePlugins(plugins);
        initializePlugins(plugins);
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
