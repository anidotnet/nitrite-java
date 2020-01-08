package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class LastWriteWinMap<Key, Value> {
    private NitriteMap<Key, LastWriteWinRegister<Value>> states;

    public LastWriteWinMap(NitriteMap<Key, LastWriteWinRegister<Value>> states) {
        this.states = states;
    }

    public void put(Key key, Value value, long timestamp) {
        LastWriteWinRegister<Value> entry = this.states.get(key);
        LastWriteWinRegister<Value> nEntry = new LastWriteWinRegister<>(value, timestamp);

        if (entry == null) {
            this.states.put(key, nEntry);
        } else {
            entry.merge(nEntry.getState());
            this.states.put(key, entry);
        }
    }

    public void remove(Key key, long timestamp) {
        LastWriteWinRegister<Value> entry = this.states.get(key);
        if (entry != null) {
            entry.set(null, timestamp);
        }
    }

    public Value get(Key key) {
        LastWriteWinRegister<Value> entry = this.states.get(key);
        if (entry != null) {
            return entry.get();
        }

        return null;
    }

    public boolean contains(Key key) {
        return this.get(key) != null;
    }

    public long size() {
        return this.filtered().size();
    }

    public Map<Key, Value> getAll() {
        return this.filtered();
    }

    private Map<Key, Value> filtered() {
        Map<Key, Value> map = new LinkedHashMap<>();
        for (KeyValuePair<Key, LastWriteWinRegister<Value>> entry : this.states.entries()) {
            if (entry.getValue().get() != null) {
                map.put(entry.getKey(), entry.getValue().get());
            }
        }
        return map;
    }

    public void merge(Map<Key, LastWriteWinRegister<Value>> states) {
        for (Map.Entry<Key, LastWriteWinRegister<Value>> entry : states.entrySet()) {
            LastWriteWinRegister<Value> tmp = this.states.get(entry.getKey());
            if (tmp == null) {
                this.states.put(entry.getKey(), entry.getValue());
            } else {
                tmp.merge(entry.getValue().getState());
            }
        }
    }

    public void merge(NitriteMap<Key, LastWriteWinRegister<Value>> states) {
        for (KeyValuePair<Key, LastWriteWinRegister<Value>> entry : states.entries()) {
            LastWriteWinRegister<Value> tmp = this.states.get(entry.getKey());
            if (tmp == null) {
                this.states.put(entry.getKey(), entry.getValue());
            } else {
                tmp.merge(entry.getValue().getState());
            }
        }
    }
}
