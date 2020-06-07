package org.dizitart.no2.store;

import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.h2.mvstore.MVMap;

import java.util.Iterator;
import java.util.Map;

import static org.dizitart.no2.common.Constants.META_MAP_NAME;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
class NitriteMVMap<Key, Value> implements NitriteMap<Key, Value> {
    private final MVMap<Key, Value> mvMap;
    private final NitriteStore nitriteStore;

    NitriteMVMap(MVMap<Key, Value> mvMap, NitriteStore nitriteStore) {
        this.mvMap = mvMap;
        this.nitriteStore = nitriteStore;
    }

    @Override
    public boolean containsKey(Key key) {
        return mvMap.containsKey(key);
    }

    @Override
    public Value get(Key key) {
        return mvMap.get(key);
    }

    @Override
    public NitriteStore getStore() {
        return nitriteStore;
    }

    @Override
    public void clear() {
        updateAttributes();
        mvMap.clear();
    }

    @Override
    public String getName() {
        return mvMap.getName();
    }

    @Override
    public ReadableStream<Value> values() {
        return ReadableStream.fromIterable(mvMap.values());
    }

    @Override
    public Value remove(Key key) {
        updateAttributes();
        return mvMap.remove(key);
    }

    @Override
    public ReadableStream<Key> keySet() {
        return ReadableStream.fromIterable(mvMap.keySet());
    }

    @Override
    public void put(Key key, Value value) {
        updateAttributes();
        mvMap.put(key, value);
    }

    @Override
    public long size() {
        return mvMap.sizeAsLong();
    }

    @Override
    public Value putIfAbsent(Key key, Value value) {
        updateAttributes();
        return mvMap.putIfAbsent(key, value);
    }

    @Override
    public ReadableStream<KeyValuePair<Key, Value>> entries() {
        return () -> new Iterator<KeyValuePair<Key, Value>>() {
            final Iterator<Map.Entry<Key, Value>> entryIterator = mvMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public KeyValuePair<Key, Value> next() {
                Map.Entry<Key, Value> entry = entryIterator.next();
                return new KeyValuePair<>(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public Key higherKey(Key key) {
        return mvMap.higherKey(key);
    }

    @Override
    public Key ceilingKey(Key key) {
        return mvMap.ceilingKey(key);
    }

    @Override
    public Key lowerKey(Key key) {
        return mvMap.lowerKey(key);
    }

    @Override
    public Key floorKey(Key key) {
        return mvMap.floorKey(key);
    }

    @Override
    public boolean isEmpty() {
        return mvMap.isEmpty();
    }

    @Override
    public void drop() {
        nitriteStore.removeMap(getName());
    }

    @Override
    public Attributes getAttributes() {
        NitriteMap<String, Attributes> metaMap = nitriteStore.openMap(META_MAP_NAME);
        if (metaMap != null && !getName().contentEquals(META_MAP_NAME)) {
            return metaMap.get(getName());
        }
        return null;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        NitriteMap<String, Attributes> metaMap = nitriteStore.openMap(META_MAP_NAME);
        if (metaMap != null && !getName().contentEquals(META_MAP_NAME)) {
            metaMap.put(getName(), attributes);
        }
    }

    private void updateAttributes() {
        if (isNullOrEmpty(getName())
            || META_MAP_NAME.equals(getName())) return;

        NitriteMap<String, Attributes> metaMap = nitriteStore.openMap(META_MAP_NAME);
        if (metaMap != null) {
            Attributes attributes = metaMap.get(getName());
            if (attributes == null) {
                attributes = new Attributes(getName());
                metaMap.put(getName(), attributes);
            }
            attributes.set(Attributes.LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
        }
    }
}
