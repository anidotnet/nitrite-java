package org.dizitart.no2.store;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.index.BoundingBox;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

import java.util.Iterator;

/**
 * @author Anindya Chatterjee
 */
class NitriteMVRTreeMap<Key extends BoundingBox, Value>
    implements NitriteRTree<Key, Value> {
    private final MVRTreeMap<Key> mvMap;

    NitriteMVRTreeMap(MVRTreeMap<Key> mvMap) {
        this.mvMap = mvMap;
    }

    @Override
    public void add(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            mvMap.add(spatialKey, key);
        }
    }

    @Override
    public void remove(Key key, NitriteId nitriteId) {
        if (nitriteId != null && nitriteId.getIdValue() != null) {
            SpatialKey spatialKey = getKey(key, Long.parseLong(nitriteId.getIdValue()));
            mvMap.remove(spatialKey);
        }
    }

    @Override
    public ReadableStream<NitriteId> findIntersectingKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findIntersectingKeys(spatialKey);
        return ReadableStream.fromIterator(new Iterator<NitriteId>() {
            @Override
            public boolean hasNext() {
                return treeCursor.hasNext();
            }

            @Override
            public NitriteId next() {
                SpatialKey next = treeCursor.next();
                return NitriteId.createId(Long.toString(next.getId()));
            }
        });
    }

    @Override
    public ReadableStream<NitriteId> findContainedKeys(Key key) {
        SpatialKey spatialKey = getKey(key, 0L);
        MVRTreeMap.RTreeCursor treeCursor = mvMap.findContainedKeys(spatialKey);
        return ReadableStream.fromIterator(new Iterator<NitriteId>() {
            @Override
            public boolean hasNext() {
                return treeCursor.hasNext();
            }

            @Override
            public NitriteId next() {
                SpatialKey next = treeCursor.next();
                return NitriteId.createId(Long.toString(next.getId()));
            }
        });
    }

    private SpatialKey getKey(Key key, long id) {
        return new SpatialKey(id, key.getMinX(),
            key.getMaxX(), key.getMinY(), key.getMaxY());
    }
}
