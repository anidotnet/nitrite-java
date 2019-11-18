package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactory {
    public static NitriteCollection getCollection(String name, NitriteConfig nitriteConfig) {
        return new NitriteCollectionImpl(name, nitriteConfig);
    }
}
