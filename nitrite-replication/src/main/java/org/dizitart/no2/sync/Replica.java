package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, ReplicationEventListener {
    private ReplicationConfig replicationConfig;
    private LocalOperation localOperation;
    private RemoteOperation remoteOperation;
    private Connection connection;
    private ReplicationEventBus eventBus;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        configure();
    }

    public void connect() {
        ConnectionConfig connectionConfig = replicationConfig.getConnectionConfig();
        ObjectMapper objectMapper = replicationConfig.getObjectMapper();
        connection = Connection.create(connectionConfig, text -> eventBus.handleMessage(objectMapper, text));

        localOperation.sendConnect(connection);
        localOperation.sendLocalChanges(connection);

        replicationConfig.getCollection().subscribe(this);
        eventBus.register(this);

        connection.open();
    }

    public void disconnect() {
        localOperation.sendDisconnect(connection);
        eventBus.deregister(this);
        try {
            connection.close();
        } catch (Exception e) {
            throw new ReplicationException("failed to close connection", e);
        }
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
                localOperation.handleInsertEvent(document, connection);
                break;
            case Remove:
                document = (Document) eventInfo.getItem();
                localOperation.handleRemoveEvent(document, connection);
                break;
            case IndexStart:
            case IndexEnd:
                break;
        }
    }

    @Override
    public void onEvent(ReplicationEvent event) {
        System.out.println("Handling server message - " + event.getMessage());
        validateEvent(event);
        if (getReplicaId().equals(event.getMessage().getMessageHeader().getSource())) {
            // ignore broadcast message
            System.out.println("Ignoring - " + event.getMessage());
            return;
        }
        remoteOperation.handleReplicationEvent(event);
    }

    public String getReplicaId() {
        return localOperation.getReplicaId();
    }

    private void configure() {
        this.eventBus = new ReplicationEventBus();
        this.localOperation = new LocalOperation(replicationConfig);
        this.remoteOperation = new RemoteOperation(replicationConfig);
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
