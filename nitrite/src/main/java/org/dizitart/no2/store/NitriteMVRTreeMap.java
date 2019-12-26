package org.dizitart.no2.store;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.index.BoundingBox;
import org.h2.mvstore.rtree.MVRTreeMap;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class NitriteMVRTreeMap<Key, Value extends BoundingBox> implements NitriteRTree<? extends BoundingBox, Value> {
    private final MVRTreeMap<Value> mvMap;
    private final NitriteStore nitriteStore;

    NitriteMVRTreeMap(MVRTreeMap<Value> mvMap, NitriteStore nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public void add(NitriteId nitriteId, Value value) {

    }

    @Override
    public void remove(NitriteId nitriteId) {

    }

    @Override
    public Set<KeyValuePair<NitriteId, Value>> entries() {
        return null;
    }

    @Override
    public ReadableStream<NitriteId> findIntersectingKeys(NitriteId nitriteId) {
        return null;
    }

    @Override
    public ReadableStream<NitriteId> findContainedKeys(NitriteId nitriteId) {
        return null;
    }
}
