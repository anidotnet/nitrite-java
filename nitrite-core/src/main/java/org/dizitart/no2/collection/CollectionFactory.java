package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactory {
    public static NitriteCollection getCollection(String name, NitriteConfig nitriteConfig) {
        NitriteStore store = nitriteConfig.getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = store.openMap(name);
        return new NitriteCollectionImpl(name, nitriteMap, nitriteConfig);
    }
}
