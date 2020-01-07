package org.dizitart.no2.store.compat.v3;

import org.h2.mvstore.MVMap;

/**
 * @author Anindya Chatterjee.
 */
class MVMapBuilder<K, V> extends MVMap.Builder<K, V> {
    public MVMapBuilder() {
        setKeyType(new NitriteDataType());
        setValueType(new NitriteDataType());
    }
}
