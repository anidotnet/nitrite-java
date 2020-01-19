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

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, ReplicationEventListener, ConnectionAware {
    private ReplicationConfig replicationConfig;
    private LocalOperation localOperation;
    private RemoteOperation remoteOperation;
    private Set<NitriteId> replicatedEntries;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        this.localOperation = new LocalOperation(replicationConfig);
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
        if (getReplicaId().equals(event.getMessage().getMessageHeader().getSource())) {
            // ignore broadcast message
            return;
        }
        trackReplicatedEntries(event);
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

    private void trackReplicatedEntries(ReplicationEvent event) {
        DataGateMessage message = event.getMessage();
        if (message instanceof ChangeMessage) {
            ChangeMessage feed = (ChangeMessage) message;
            for (Document change : feed.getFeed().getChanges()) {
                replicatedEntries.add(change.getId());
            }

            for (Long id : feed.getFeed().getTombstones().keySet()) {
                replicatedEntries.add(NitriteId.createId(id));
            }
        }
    }
}
