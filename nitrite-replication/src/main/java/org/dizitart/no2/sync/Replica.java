package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.connection.ConnectionAware;
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
        localOperation.sendConnect();
        localOperation.sendLocalChanges();

        replicationConfig.getCollection().subscribe(this);
        ReplicationEventBus.getInstance().register(this);

        getConnection().sendAndReceive();
    }

    public void disconnect() {
        localOperation.sendDisconnect();
        ReplicationEventBus.getInstance().deregister(this);
    }

    @Override
    public String getName() {
        return replicationConfig.getCollection().getName();
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        Document document;
        switch(eventInfo.getEventType()) {
            case Insert:
            case Update:
                document = (Document) eventInfo.getItem();
                localOperation.handleInsertEvent(document);
                break;
            case Remove:
                document = (Document) eventInfo.getItem();
                localOperation.handleRemoveEvent(document);
                break;
            case IndexStart:
            case IndexEnd:
                break;
        }
    }

    @Override
    public void onEvent(ReplicationEvent event) {
        validateEvent(event);
        if (getReplicaId().equals(event.getMessage().getMessageHeader().getSource())) {
            // ignore broadcast message
            return;
        }
        remoteOperation.handleReplicationEvent(event);
    }

    @Override
    public ReplicationConfig getConfig() {
        return replicationConfig;
    }

    public String getReplicaId() {
        return localOperation.getReplicaId();
    }

    private void validateEvent(ReplicationEvent event) {
        if (event == null) {
            throw new ReplicationException("a null event received for " + getReplicaId());
        } else if (event.getMessage() == null) {
            throw new ReplicationException("a null message received for " + getReplicaId());
        } else if (event.getMessage().getMessageHeader() == null) {
            throw new ReplicationException("invalid message info received for " + getReplicaId());
        } else if (StringUtils.isNullOrEmpty(event.getMessage().getMessageHeader().getCollection())) {
            throw new ReplicationException("invalid message info received for " + getReplicaId());
        } else if (event.getMessage().getMessageHeader().getMessageType() == null) {
            throw new ReplicationException("invalid message type received for " + getReplicaId());
        }
    }
}
