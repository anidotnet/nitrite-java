package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.event.EventListener;
import org.dizitart.no2.sync.event.EventType;
import org.dizitart.no2.sync.event.ReplicationEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.filters.Filter.byId;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class LastWriteWinMap {
    private NitriteCollection collection;
    private NitriteMap<NitriteId, Long> tombstones;

    public LastWriteWinMap(NitriteCollection collection, NitriteConfig config) {
        this.collection = collection;
        this.tombstones = createTombstones(collection, config);
    }

    public void put(Document value) {
        NitriteId key = value.getId();

        Document entry = collection.getById(key);
        if (entry == null) {
            if (tombstones.containsKey(key)) {
                Long tombstoneTime = tombstones.get(key);
                Long docModifiedTime = value.getLastModifiedSinceEpoch();

                if (docModifiedTime >= tombstoneTime) {
                    collection.insert(value);
                }
            } else {
                collection.insert(value);
            }
        } else {
            Long oldTime = entry.getLastModifiedSinceEpoch();
            Long newTime = value.getLastModifiedSinceEpoch();

            if (newTime > oldTime) {
                collection.remove(byId(key));
                collection.insert(value);
            }
        }
    }

    public void remove(NitriteId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            collection.remove(byId(key));
            tombstones.put(key, timestamp);
        }
    }

    public Document get(NitriteId key) {
        return collection.getById(key);
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

    public void subscribe(EventListener eventListener) {
        eventBus.register(eventListener);
    }

    public void unsubscribe(EventListener eventListener) {
        eventBus.deregister(eventListener);
    }

    private void alert(EventType eventType, LastWriteWinRegister<Value> entry) {
        ReplicationEvent event = new ReplicationEvent();
        event.setEventType(eventType);
        event.setEventInfo(entry);
        eventBus.post(event);
    }

    private NitriteMap<NitriteId, Long> createTombstones(NitriteCollection collection, NitriteConfig config) {
        return null;
    }

    private static class ReplicationEventBus extends NitriteEventBus<ReplicationEvent, EventListener> {
        @Override
        public void post(ReplicationEvent eventInfo) {
            for (final EventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(eventInfo));
            }
        }

        @Override
        protected ExecutorService getEventExecutor() {
            return ExecutorServiceManager.syncExecutor();
        }
    }
}
