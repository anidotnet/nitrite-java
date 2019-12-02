package org.dizitart.no2;

import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.plugin.PluginManager;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;

/**
 * A class to configure {@link Nitrite} database.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
public abstract class NitriteConfig {
    private boolean configured = false;

    /**
     * Gets the embedded field separator character. Default value
     * is `.` unless set explicitly.
     *
     * @returns the embedded field separator character.
     * */
    @Getter
    private static String fieldSeparator;

    @Getter(AccessLevel.PACKAGE)
    private final PluginManager pluginManager;

    /**
     * Gets the thread pool shutdown timeout in seconds. Default value
     * is 5s unless set explicitly.
     *
     * @returns the thread pool shutdown timeout in seconds.
     * */
    @Getter
    private Integer poolShutdownTimeout;

    /**
     * Gets the {@link NitriteStore} configuration.
     *
     * @returns the {@link NitriteStore} configuration.
     * */
    @Getter
    private StoreConfig storeConfig;

    private NitriteConfig() {
        pluginManager = new PluginManager(this);
    }

    /**
     * Creates a new {@link NitriteConfig} instance.
     *
     * @return the {@link NitriteConfig} instance.
     */
    public static NitriteConfig create() {
        NitriteConfig config = new NitriteConfig(){};
        config.fieldSeparator(".");
        config.poolShutdownTimeout(5);
        return config;
    }

    /**
     * Sets the embedded field separator character. Default value
     * is `.`
     *
     * @param separator the separator
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig fieldSeparator(String separator) {
        if (configured) {
            throw new InvalidOperationException("cannot change the separator after database" +
                " initialization");
        }
        NitriteConfig.fieldSeparator = separator;
        return this;
    }

    /**
     * Sets the thread pool shutdown timeout in seconds. Default value
     * is 5s.
     *
     * @param timeout the timeout
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig poolShutdownTimeout(int timeout) {
        if (configured) {
            throw new InvalidOperationException("cannot change pool shutdown timeout after database" +
                " initialization");
        }
        this.poolShutdownTimeout = timeout;
        return this;
    }

    /**
     * Sets the configuration for {@link NitriteStore}.
     *
     * @param storeConfig the {@link StoreConfig} instance.
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig storeConfig(StoreConfig storeConfig) {
        if (configured) {
            throw new InvalidOperationException("cannot change store config after database" +
                " initialization");
        }
        this.storeConfig = storeConfig;
        return this;
    }

    /**
     * Auto configures nitrite database with default configuration values and
     * default built-in plugins.
     *
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig autoConfigure() {
        if (configured) {
            throw new InvalidOperationException("cannot execute autoconfigure after database" +
                " initialization");
        }
        pluginManager.findAndLoadPlugins();
        return this;
    }

    /**
     * Loads {@link NitritePlugin} instances.
     *
     * @param plugins the {@link NitritePlugin} instances.
     * @return the {@link NitriteConfig} instance.
     */
    public NitriteConfig load(NitritePlugin...plugins) {
        if (configured) {
            throw new InvalidOperationException("cannot load plugin after database" +
                " initialization");
        }
        pluginManager.load(plugins);
        return this;
    }

    /**
     * Loads plugins from {@link NitritePlugin} classes.
     *
     * @param plugins the {@link NitritePlugin} classes.
     * @return the {@link NitriteConfig} instance.
     */
    @SafeVarargs
    public final NitriteConfig load(Class<? extends NitritePlugin>... plugins) {
        if (configured) {
            throw new InvalidOperationException("cannot load plugin after database" +
                " initialization");
        }
        pluginManager.load(plugins);
        return this;
    }

    /**
     * Finds an {@link Indexer} by indexType.
     *
     * @param indexType the type of {@link Indexer} to find.
     * @return the {@link Indexer}
     */
    public Indexer findIndexer(String indexType) {
        return pluginManager.getIndexerMap().get(indexType);
    }

    /**
     * Gets the {@link NitriteMapper} instance.
     *
     * @return the {@link NitriteMapper}
     */
    public NitriteMapper nitriteMapper() {
        return pluginManager.getNitriteMapper();
    }

    /**
     * Gets {@link NitriteStore} instance.
     *
     * @return the {@link NitriteStore}
     */
    public NitriteStore getNitriteStore() {
        return pluginManager.getNitriteStore();
    }

    void initialized() {
        this.configured = true;
    }
}
