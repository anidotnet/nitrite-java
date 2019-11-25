package org.dizitart.no2;

import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.plugin.NitritePlugin;
import org.dizitart.no2.plugin.PluginManager;
import org.dizitart.no2.store.NitriteStore;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
public abstract class NitriteConfig {
    @Getter
    private static String fieldSeparator;

    @Getter
    private boolean readOnly;

    @Getter(AccessLevel.PACKAGE)
    private final PluginManager pluginManager;

    @Getter
    private Integer poolShutdownTimeout;

    private NitriteConfig() {
        pluginManager = new PluginManager(this);
    }

    public static NitriteConfig create() {
        NitriteConfig config = new NitriteConfig(){};
        config.fieldSeparator(".");
        config.readOnly(false);
        config.poolShutdownTimeout(5);
        return config;
    }


    public NitriteConfig fieldSeparator(String separator) {
        NitriteConfig.fieldSeparator = separator;
        return this;
    }

    public NitriteConfig readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public NitriteConfig poolShutdownTimeout(int timeout) {
        this.poolShutdownTimeout = timeout;
        return this;
    }

    public NitriteConfig autoConfigure() {
        // load plugins using Class.forName("...")
        return this;
    }

    public NitriteConfig load(NitritePlugin... plugins) {
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
}
