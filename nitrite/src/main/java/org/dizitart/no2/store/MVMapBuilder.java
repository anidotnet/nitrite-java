package org.dizitart.no2.store;

import org.h2.mvstore.MVMap;

/**
 * @author Anindya Chatterjee.
 */
class MVMapBuilder<K, V> extends MVMap.Builder<K, V> {
    MVMapBuilder() {
        setKeyType(new NitriteDataType());
        setValueType(new NitriteDataType());
    }
}
