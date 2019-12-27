package org.dizitart.no2.store;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteRTree<Key, Value> {
    void add(Key key, NitriteId nitriteId);

    void remove(Key key, NitriteId nitriteId);

    ReadableStream<NitriteId> findIntersectingKeys(Key key);

    ReadableStream<NitriteId> findContainedKeys(Key key);
}
