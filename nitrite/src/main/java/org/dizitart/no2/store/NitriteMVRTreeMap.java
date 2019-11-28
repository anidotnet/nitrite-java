package org.dizitart.no2.store;

import org.dizitart.no2.common.ReadableStream;
import org.h2.mvstore.MVMap;

/**
 * @author Anindya Chatterjee
 */
public class NitriteMVRTreeMap<Key, Value> extends NitriteMVMap<Key, Value> implements NitriteRTree<Key, Value> {
    NitriteMVRTreeMap(MVMap<Key, Value> mvMap, NitriteStore nitriteStore) {
        super(mvMap, nitriteStore);
    }

    @Override
    public void add(Key key, Value value) {

    }

    @Override
    public ReadableStream<Key> findIntersectingKeys(Key key) {
        return null;
    }

    @Override
    public ReadableStream<Key> findContainedKeys(Key key) {
        return null;
    }
}
