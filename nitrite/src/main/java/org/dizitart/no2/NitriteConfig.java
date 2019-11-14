package org.dizitart.no2;

import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.indexer.Indexer;
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
    private String fieldSeparator;

    @Getter(AccessLevel.PACKAGE)
    private final PluginManager pluginManager;

    private NitriteConfig() {
        pluginManager = new PluginManager();
    }

    public static NitriteConfig create() {
        NitriteConfig config = new NitriteConfig(){};
        config.fieldSeparator(".");
        // TODO: load plugins here config.load();
        return config;
    }


    public NitriteConfig fieldSeparator(String separator) {
        this.fieldSeparator = separator;
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
