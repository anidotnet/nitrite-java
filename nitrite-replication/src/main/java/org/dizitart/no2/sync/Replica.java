package org.dizitart.no2.sync;

import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.connection.ConnectionPool;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener {
    private ReplicationConfig replicationConfig;
    private LastWriteWinMap crdt;
    private ReplicationOperation operation;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        this.operation = new ReplicationOperation(replicationConfig);
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        operation.handleCollectionEvent(eventInfo);
    }

    public void connect() {
        Connection connection = ConnectionPool.create().getConnection(replicationConfig.getConnectionConfig());
        connection.open();

    }
}
