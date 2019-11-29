package org.dizitart.no2.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.collection.index.NonUniqueIndexer;
import org.dizitart.no2.collection.index.UniqueIndexer;
import org.dizitart.no2.exceptions.PluginException;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

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

        load(new UniqueIndexer());
        load(new NonUniqueIndexer());
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
                throw new PluginException(errorMessage("multiple NitriteStore found",
                    PE_MULTIPLE_STORE_FOUND));
            }
            this.nitriteStore = (NitriteStore) plugin;
        } else if (plugin instanceof StoreConfig) {
            if (storeConfig != null) {
                throw new PluginException(errorMessage("multiple StoreConfig found",
                    PE_MULTIPLE_STORE_CONFIG_FOUND));
            }
            this.storeConfig = (StoreConfig) plugin;
        }
    }

    private void loadIfNitriteMapper(NitritePlugin plugin) {
        if (plugin instanceof NitriteMapper) {
            if (nitriteMapper != null) {
                throw new PluginException(errorMessage("multiple NitriteMapper found",
                    PE_MULTIPLE_MAPPER_FOUND));
            }
            this.nitriteMapper = (NitriteMapper) plugin;
        }
    }

    private synchronized void loadIfIndexer(NitritePlugin plugin) {
        if (plugin instanceof Indexer) {
            Indexer indexer = (Indexer) plugin;
            if (indexerMap.containsKey(indexer.getIndexType())) {
                throw new PluginException(errorMessage("multiple Indexer found for type "
                    + indexer.getIndexType(), PE_MULTIPLE_INDEXER_FOUND));
            }
            this.indexers.add((Indexer) plugin);
            this.indexerMap.put(indexer.getIndexType(), indexer);
        }
    }
}
