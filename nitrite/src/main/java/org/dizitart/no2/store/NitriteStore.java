package org.dizitart.no2.store;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.plugin.NitritePlugin;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteStore extends NitritePlugin {
    NitriteCollection getCollection(String name);

    boolean isClosed();

    // populate existing collection names when loading
    Set<String> getCollectionNames();
}
