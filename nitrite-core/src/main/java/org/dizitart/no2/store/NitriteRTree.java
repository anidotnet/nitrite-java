package org.dizitart.no2.store;

import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.index.BoundingBox;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteRTree<Key extends BoundingBox, Value> {
    void add(Key key, Value value);

    void remove(Key key);

    Set<KeyValuePair<Key, Value>> entries();

    ReadableStream<Key> findIntersectingKeys(Key key);

    ReadableStream<Key> findContainedKeys(Key key);
}
