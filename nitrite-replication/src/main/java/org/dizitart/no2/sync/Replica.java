package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.connection.ConnectionPool;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.event.EventListener;
import org.dizitart.no2.sync.event.ReplicationEvent;

import static org.dizitart.no2.common.Constants.REPLICATOR;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, EventListener {
    private ConnectionConfig connectionConfig;
    private LastWriteWinMap crdt;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(NitriteCollection collection, NitriteMap<NitriteId, Long> tombstones) {
        this.crdt = new LastWriteWinMap(collection, tombstones);
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        Document document = (Document) eventInfo.getItem();
        switch (eventInfo.getEventType()) {
            case Insert:
            case Update:
                crdt.put(document.getId(), document, System.currentTimeMillis(), false);
                break;
            case Remove:
                if (!REPLICATOR.equalsIgnoreCase(document.getSource())) {

                }
                crdt.remove(document.getId(), System.currentTimeMillis(), false);
                break;
            case IndexStart:
            case IndexEnd:
                // nothing required
                break;
        }
    }


    @Override
    public void onEvent(ReplicationEvent event) {

    }

    public void connect() {
        Connection connection = ConnectionPool.create().getConnection(connectionConfig);
        connection.open();

    }

    void connectionConfig(ConnectionConfig config) {
        this.connectionConfig = config;
    }
}
