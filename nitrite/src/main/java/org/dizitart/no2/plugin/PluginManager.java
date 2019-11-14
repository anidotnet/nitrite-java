package org.dizitart.no2.plugin;

import org.dizitart.no2.indexer.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
public class PluginManager {
    public void load(NitritePlugin[] plugins) {

    }

    public Set<Indexer> getIndexers() {
        return null;
    }

    public NitriteMapper getNitriteMapper() {
        return null;
    }

    public NitriteStore getNitriteStore() {
        return null;
    }
}
