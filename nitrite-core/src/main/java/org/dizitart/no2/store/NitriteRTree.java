package org.dizitart.no2.store;

import org.dizitart.no2.common.ReadableStream;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteRTree<Key, Value> extends NitriteMap<Key, Value> {
    void add(Key key, Value value);

    ReadableStream<Key> findIntersectingKeys(Key key);

    ReadableStream<Key> findContainedKeys(Key key);

}
