package org.dizitart.no2.sync;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

import java.util.UUID;

import static org.dizitart.no2.collection.meta.Attributes.*;
import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;

/**
 * @author Anindya Chatterjee
 */
interface ReplicationOperation {
    NitriteCollection getCollection();

    default Attributes getAttributes() {
        Attributes attributes = getCollection().getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
            saveAttributes(attributes);
        }
        return attributes;
    }

    default void saveAttributes(Attributes attributes) {
        getCollection().setAttributes(attributes);
    }

    default Long getLastSyncTime() {
        Attributes attributes = getAttributes();
        String syncTimeStr = attributes.get(LAST_SYNCED);
        if (StringUtils.isNullOrEmpty(syncTimeStr)) {
            return Long.MIN_VALUE;
        } else {
            return Long.parseLong(syncTimeStr);
        }
    }

    default LastWriteWinMap createReplicatedDataType() {
        Attributes attributes = getAttributes();
        String tombstoneName = getTombstoneName(attributes);
        saveAttributes(attributes);

        NitriteStore store = getCollection().getStore();
        NitriteMap<NitriteId, Long> tombstone = store.openMap(tombstoneName);
        return new LastWriteWinMap(getCollection(), tombstone);
    }

    default String getTombstoneName(Attributes attributes) {
        String tombstoneName = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(tombstoneName)) {
            tombstoneName = getCollection().getName()
                + INTERNAL_NAME_SEPARATOR + TOMBSTONE
                + INTERNAL_NAME_SEPARATOR + UUID.randomUUID().toString();
            attributes.set(TOMBSTONE, tombstoneName);
        }
        return tombstoneName;
    }

    default void saveLastSyncTime() {
        Attributes attributes = getAttributes();
        attributes.set(LAST_SYNCED, Long.toString(System.currentTimeMillis()));
        saveAttributes(attributes);
    }
}
