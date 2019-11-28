package org.dizitart.no2;

import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.plugin.NitritePluginContainer;
import org.dizitart.no2.plugin.PluginManager;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.IOE_DATABASE_ALREADY_INITIALIZED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
public abstract class NitriteConfig {
    private boolean configured = false;

    @Getter
    private static String fieldSeparator;

    @Getter(AccessLevel.PACKAGE)
    private final PluginManager pluginManager;

    @Getter
    private Integer poolShutdownTimeout;

    @Getter
    private StoreConfig storeConfig;

    private NitriteConfig() {
        pluginManager = new PluginManager(this);
    }

    public static NitriteConfig create() {
        NitriteConfig config = new NitriteConfig(){};
        config.fieldSeparator(".");
        config.poolShutdownTimeout(5);
        return config;
    }

    public NitriteConfig fieldSeparator(String separator) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change the separator after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        NitriteConfig.fieldSeparator = separator;
        return this;
    }

    public NitriteConfig poolShutdownTimeout(int timeout) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change pool shutdown timeout after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.poolShutdownTimeout = timeout;
        return this;
    }

    public NitriteConfig storeConfig(StoreConfig storeConfig) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change store config after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.storeConfig = storeConfig;
        return this;
    }

    public NitriteConfig autoConfigure() {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot execute autoconfigure after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        findAndLoadPlugins();
        return this;
    }

    public NitriteConfig load(NitritePlugin...plugins) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot load plugin after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        pluginManager.load(plugins);
        return this;
    }

    public NitriteConfig load(Class<? extends NitritePlugin>... plugins) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot load plugin after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        pluginManager.load(plugins);
        return this;
    }

    public Set<Indexer> getIndexers() {
        return pluginManager.getIndexers();
    }

    public NitriteMapper nitriteMapper() {
        return pluginManager.getNitriteMapper();
    }

    public NitriteStore getNitriteStore() {
        return pluginManager.getNitriteStore();
    }

    public StoreConfig getStoreConfig() {
        if (storeConfig == null) {
            storeConfig = pluginManager.getStoreConfig();
        }
        return storeConfig;
    }

    void initialized() {
        this.configured = true;
    }

    private void findAndLoadPlugins() {
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
    }
}
