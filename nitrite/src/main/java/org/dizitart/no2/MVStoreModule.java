package org.dizitart.no2;

import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.module.NitritePlugin;
import org.dizitart.no2.store.NitriteMVStore;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class MVStoreModule implements NitriteModule {
    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(new NitriteMVStore());
    }
}
