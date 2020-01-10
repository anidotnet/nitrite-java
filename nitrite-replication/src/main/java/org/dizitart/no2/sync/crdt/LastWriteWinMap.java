package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.event.EventListener;
import org.dizitart.no2.sync.event.EventType;
import org.dizitart.no2.sync.event.ReplicationEvent;

import java.util.Map;

import static org.dizitart.no2.common.Constants.DOC_SOURCE;
import static org.dizitart.no2.common.Constants.REPLICATOR;
import static org.dizitart.no2.filters.Filter.byId;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class LastWriteWinMap {
    private NitriteCollection collection;
    private NitriteMap<NitriteId, Long> tombstones;

    public LastWriteWinMap(NitriteCollection collection, NitriteMap<NitriteId, Long> tombstones) {
        this.collection = collection;
        this.tombstones = tombstones;
    }

    public void put(Document value) {
        NitriteId key = value.getId();

        Document entry = collection.getById(key);
        if (entry == null) {
            if (tombstones.containsKey(key)) {
                Long tombstoneTime = tombstones.get(key);
                Long docModifiedTime = value.getLastModifiedSinceEpoch();

                if (docModifiedTime >= tombstoneTime) {
                    value.put(DOC_SOURCE, REPLICATOR);
                    collection.insert(value);
                }
            } else {
                value.put(DOC_SOURCE, REPLICATOR);
                collection.insert(value);
            }
        } else {
            Long oldTime = entry.getLastModifiedSinceEpoch();
            Long newTime = value.getLastModifiedSinceEpoch();

            if (newTime > oldTime) {
                entry.put(DOC_SOURCE, REPLICATOR);
                collection.remove(byId(key));

                value.put(DOC_SOURCE, REPLICATOR);
                collection.insert(value);
            }
        }
    }

    public void remove(NitriteId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            entry.put(DOC_SOURCE, REPLICATOR);
            collection.remove(byId(key));
            tombstones.put(key, timestamp);
        }
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
