package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class ReplicationOperation {
    private static final String TOMBSTONE = "tombstone";

    private ReplicationConfig replicationConfig;
    private NitriteCollection collection;
    private LastWriteWinMap crdt;
    private DataGateClient client;

    public ReplicationOperation(ReplicationConfig replicationConfig) {
        this.replicationConfig = replicationConfig;
        this.collection = replicationConfig.getCollection();
        this.crdt = createReplicatedDataType();
        this.client = createClient();
    }

    public void handleCollectionEvent(CollectionEventInfo<?> eventInfo) {
        Document document = (Document) eventInfo.getItem();
        switch (eventInfo.getEventType()) {

        }
    }

    private LastWriteWinMap createReplicatedDataType() {
        Attributes attributes = getAttributes();
        String tombstoneName = getTombstoneName(attributes);
        saveAttributes(attributes);

        NitriteStore store = collection.getStore();
        NitriteMap<NitriteId, Long> tombstone = store.openMap(tombstoneName);
        return new LastWriteWinMap(collection, tombstone);
    }

    private String getTombstoneName(Attributes attributes) {
        String replica = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(replica)) {
            replica = UUID.randomUUID().toString();
            attributes.set(TOMBSTONE, replica);
        }
        return replica;
    }

    private Attributes getAttributes() {
        Attributes attributes = collection.getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
        }
        return attributes;
    }

    private void saveAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }

    private DataGateClient createClient() {
        DataGateClient client = new DataGateClient()
    }
}
