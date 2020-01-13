package org.dizitart.no2.sync;

import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.connection.ConnectionPool;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, ReplicationEventListener {
    private ReplicationConfig replicationConfig;
    private ReplicationOperation operation;
    private Connection connection;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        this.operation = new ReplicationOperation(replicationConfig);
    }

    public void connect() {
        connection = ConnectionPool.getInstance().getConnection(replicationConfig.getConnectionConfig());
        connection.open();

        operation.sendLocalChanges(connection);

        replicationConfig.getCollection().subscribe(this);
        ReplicationEventBus.getInstance().register(this);

        connection.sendAndReceive();

        //TODO: create LocalOperations & RemoteOperations and divide the tasks
    }

    public void disconnect() {
        replicationConfig.getCollection().unsubscribe(this);
        ReplicationEventBus.getInstance().deregister(this);
    }

    @Override
    public String getName() {
        return replicationConfig.getCollection().getName();
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        operation.handleCollectionEvent(connection, eventInfo);
    }

    @Override
    public void onEvent(ReplicationEvent event) {
        operation.handleReplicationEvent(event);
    }
}
