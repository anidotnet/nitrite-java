package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactory {
    public static NitriteCollection getCollection(String name, NitriteConfig nitriteConfig) {
        notNull(nitriteConfig, "configuration is null while creating collection");
        notEmpty(name, "collection name is null or empty");

        NitriteStore store = nitriteConfig.getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = store.openMap(name);
        return new NitriteCollectionImpl(name, nitriteMap, nitriteConfig);
    }
}
