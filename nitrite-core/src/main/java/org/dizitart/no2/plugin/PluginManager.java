package org.dizitart.no2.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@Getter
public class PluginManager {
    private Map<String, Indexer> indexerMap;
    private Set<Indexer> indexers;
    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;
    private NitriteConfig nitriteConfig;

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
                    pluginConstructor.setAccessible(true);
                    NitritePlugin nitritePlugin = pluginConstructor.newInstance();
                    load(nitritePlugin);
                } catch (Throwable t) {
                    log.error("Error while loading plugin " + plugin.getName(), t);
                    throw new PluginException("failed to load plugin " + plugin.getName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void findAndLoadPlugins() {
        Package[] packages = Package.getPackages();
        for (Package p : packages) {
            Annotation[] annotations = p.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(NitritePluginContainer.class)) {
                    NitritePluginContainer container = (NitritePluginContainer) annotation;
                    Class<? extends NitritePlugin>[] plugins = container.plugins();
                    load(plugins);
                }
            }
        }

        try {
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.UniqueIndexer"));
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.NonUniqueIndexer"));
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.NitriteTextIndexer"));
        } catch (ClassNotFoundException e) {
            log.error("Error while loading default plugin", e);
            throw new PluginException("error while loading default plugin", e);
        }
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
            if (nitriteStore != null) {
                throw new PluginException("multiple NitriteStore found");
            }
            this.nitriteStore = (NitriteStore) plugin;
        }
    }

    private void loadIfNitriteMapper(NitritePlugin plugin) {
        if (plugin instanceof NitriteMapper) {
            if (nitriteMapper != null) {
                throw new PluginException("multiple NitriteMapper found");
            }
            this.nitriteMapper = (NitriteMapper) plugin;
        }
    }

    private synchronized void loadIfIndexer(NitritePlugin plugin) {
        if (plugin instanceof Indexer) {
            Indexer indexer = (Indexer) plugin;
            if (indexerMap.containsKey(indexer.getIndexType())) {
                throw new PluginException("multiple Indexer found for type "
                    + indexer.getIndexType());
            }
            this.indexers.add((Indexer) plugin);
            this.indexerMap.put(indexer.getIndexType(), indexer);
        }
    }
}
