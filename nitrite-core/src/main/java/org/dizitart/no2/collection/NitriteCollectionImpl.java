package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee.
 */
class NitriteCollectionImpl implements NitriteCollection {
    public NitriteCollectionImpl(String name, NitriteConfig nitriteConfig) {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }
}
