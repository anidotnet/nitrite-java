package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;

import java.util.Map;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.FluentFilter.where;

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

    public void merge(LastWriteWinState snapshot) {
        if (snapshot.getChanges() != null) {
            for (Document entry : snapshot.getChanges()) {
                put(entry);
            }
        }

        if (snapshot.getTombstones() != null) {
            for (Map.Entry<Long, Long> entry : snapshot.getTombstones().entrySet()) {
                remove(NitriteId.createId(entry.getKey()), entry.getValue());
            }
        }
    }

    public LastWriteWinState getChangesSince(Long since, int offset, int size) {
        LastWriteWinState state = new LastWriteWinState();

        DocumentCursor cursor = collection.find(where(DOC_MODIFIED).gte(since)).skipLimit(offset, size);
        state.getChanges().addAll(cursor.toSet());

        if (offset == 0) {
            // don't repeat for other offsets
            for (KeyValuePair<NitriteId, Long> entry : tombstones.entries()) {
                Long timestamp = entry.getValue();
                if (timestamp >= since) {
                    state.getTombstones().put(entry.getKey().getIdValue(), entry.getValue());
                }
            }
        }

        return state;
    }

    private void put(Document value) {
        if (value != null) {
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
                    collection.remove(entry);

                    value.put(DOC_SOURCE, REPLICATOR);
                    collection.insert(value);
                }
            }
        }
    }

    private void remove(NitriteId key, long timestamp) {
        Document entry = collection.getById(key);
        if (entry != null) {
            entry.put(DOC_SOURCE, REPLICATOR);
            collection.remove(entry);
            tombstones.put(key, timestamp);
        }
    }
}
