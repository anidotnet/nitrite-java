package org.dizitart.no2.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@Getter
public class PluginManager {
    private Map<String, Indexer> indexerMap;
    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;
    private NitriteConfig nitriteConfig;

    public PluginManager(NitriteConfig nitriteConfig) {
        this.indexerMap = new HashMap<>();
        this.nitriteConfig = nitriteConfig;
    }

    public void load(NitritePlugin... plugins) {
        populatePlugins(plugins);
    }

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
            loadInternalPlugins();
        } catch (Exception e) {
            log.error("Error while loading internal plugins", e);
            throw new PluginException("error while loading internal plugins", e);
        }
    }

    public void initializePlugins() {
        if (indexerMap != null && !indexerMap.isEmpty()) {
            for (Indexer indexer : indexerMap.values()) {
                initializePlugin(indexer);
            }
        }

        if (nitriteMapper != null) {
            initializePlugin(nitriteMapper);
        }

        if (nitriteStore != null) {
            initializePlugin(nitriteStore);
        }
    }

    @SafeVarargs
    private final void load(Class<? extends NitritePlugin>... pluginTypes) {
        if (pluginTypes != null) {
            for (Class<? extends NitritePlugin> pluginType : pluginTypes) {
                try {
                    if (NitriteMapper.class.isAssignableFrom(pluginType) && nitriteMapper != null) continue;
                    if (NitriteStore.class.isAssignableFrom(pluginType) && nitriteStore != null) continue;

                    Constructor<? extends NitritePlugin> pluginConstructor = pluginType.getDeclaredConstructor();
                    pluginConstructor.setAccessible(true);
                    NitritePlugin nitritePlugin = pluginConstructor.newInstance();
                    load(nitritePlugin);
                } catch (Throwable t) {
                    log.error("Error while loading plugin " + pluginType.getName(), t);
                    throw new PluginException("failed to load plugin " + pluginType.getName());
                }
            }
        }
    }

    private void initializePlugin(NitritePlugin plugin) {
        plugin.initialize(nitriteConfig);
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
            this.indexerMap.put(indexer.getIndexType(), indexer);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadInternalPlugins() throws Exception {
        if (!indexerMap.containsKey(IndexType.Unique)) {
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.UniqueIndexer"));
        }

        if (!indexerMap.containsKey(IndexType.NonUnique)) {
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.NonUniqueIndexer"));
        }

        if (!indexerMap.containsKey(IndexType.Fulltext)) {
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.index.NitriteTextIndexer"));
        }

        if (nitriteMapper == null) {
            load((Class<? extends NitritePlugin>) Class.forName("org.dizitart.no2.mapper.MappableMapper"));
        }
    }
}
