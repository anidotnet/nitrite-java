package org.dizitart.no2.spatial;

import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.module.NitritePlugin;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class SpatialModule implements NitriteModule {
    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(new SpatialIndexer());
    }
}
