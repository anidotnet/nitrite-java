package org.dizitart.no2.sync;

import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, ReplicationEventListener, ConnectionAware {
    private ReplicationConfig replicationConfig;
    private LocalOperation localOperation;
    private RemoteOperation remoteOperation;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        this.localOperation = new LocalOperation(replicationConfig);
        this.remoteOperation = new RemoteOperation(replicationConfig);
    }

    public void connect() {
        localOperation.sendLocalChanges();

        replicationConfig.getCollection().subscribe(this);
        ReplicationEventBus.getInstance().register(this);

        getConnection().sendAndReceive();
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
        localOperation.handleCollectionEvent(eventInfo);
    }

    @Override
    public void onEvent(ReplicationEvent event) {
        remoteOperation.handleReplicationEvent(event);
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return replicationConfig.getConnectionConfig();
    }
}
