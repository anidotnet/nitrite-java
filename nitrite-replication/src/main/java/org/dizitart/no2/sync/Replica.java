package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.connection.ConnectionPool;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.event.EventListener;
import org.dizitart.no2.sync.event.ReplicationEvent;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, EventListener {
    private ConnectionConfig connectionConfig;
    private LastWriteWinMap<NitriteId, Document> crdt;
    private NitriteCollection collection;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(NitriteCollection collection) {
        this.collection = collection;
        this.crdt = new LastWriteWinMap<>(collection);
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
    }

    void connectionConfig(ConnectionConfig config) {
        this.connectionConfig = config;
    }
}
