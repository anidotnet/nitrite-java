package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.message.ChangeMessage;
import org.dizitart.no2.sync.message.DataGateMessage;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, ReplicationEventListener, ConnectionAware {
    private ReplicationConfig replicationConfig;
    private LocalOperation localOperation;
    private RemoteOperation remoteOperation;
    private Set<NitriteId> replicatedEntries;
    private String replicaId;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicaId = UUID.randomUUID().toString();
        this.replicationConfig = config;
        this.localOperation = new LocalOperation(replicaId, replicationConfig);
        this.remoteOperation = new RemoteOperation(replicationConfig);
        this.replicatedEntries = new LinkedHashSet<>();
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
        replicationConfig.getCollection().unsubscribe(this);
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
                if (replicatedEntries.contains(document.getId())) {
                    replicatedEntries.remove(document.getId());
                    return;
                } else {
                    localOperation.handleInsertEvent(document);
                }
                break;
            case Remove:
                document = (Document) eventInfo.getItem();
                if (replicatedEntries.contains(document.getId())) {
                    replicatedEntries.remove(document.getId());
                    return;
                } else {
                    localOperation.handleRemoveEvent(document);
                }
                break;
            case IndexStart:
            case IndexEnd:
                break;
        }
    }

    @Override
    public void onEvent(ReplicationEvent event) {
        validateEvent(event);
        trackReplicatedEntries(event);
        remoteOperation.handleReplicationEvent(event);
    }

    @Override
    public ReplicationConfig getConfig() {
        return replicationConfig;
    }

    private void validateEvent(ReplicationEvent event) {
        if (event == null) {
            throw new ReplicationException("null event received for " + replicaId);
        } else if (event.getMessage() == null) {
            throw new ReplicationException("null message received for " + replicaId);
        } else if (event.getMessage().getMessageInfo() == null) {
            throw new ReplicationException("invalid message info received for " + replicaId);
        } else if (StringUtils.isNullOrEmpty(event.getMessage().getMessageInfo().getCollection())) {
            throw new ReplicationException("invalid message info received for " + replicaId);
        } else if (event.getMessage().getMessageInfo().getMessageType() == null) {
            throw new ReplicationException("invalid message type received for " + replicaId);
        }
    }

    private void trackReplicatedEntries(ReplicationEvent event) {
        DataGateMessage message = event.getMessage();
        if (message instanceof ChangeMessage) {
            ChangeMessage feed = (ChangeMessage) message;
            for (Document change : feed.getChanges().getChanges()) {
                replicatedEntries.add(change.getId());
            }
            replicatedEntries.addAll(feed.getChanges().getTombstones().keySet());
        }
    }
}
