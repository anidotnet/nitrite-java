package org.dizitart.no2.sync;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.message.Connect;
import org.dizitart.no2.sync.message.Disconnect;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;
import static org.dizitart.no2.sync.event.ReplicationEventType.Started;
import static org.dizitart.no2.sync.event.ReplicationEventType.Stopped;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Getter
public class ReplicationTemplate implements ReplicationOperation {
    private Config config;
    private MessageFactory messageFactory;
    private MessageTemplate messageTemplate;
    private LastWriteWinMap crdt;
    private FeedJournal feedJournal;

    @Getter(AccessLevel.NONE)
    private BatchChangeScheduler batchChangeScheduler;

    @Getter(AccessLevel.NONE)
    private String replicaId;

    @Getter(AccessLevel.NONE)
    private ReplicaChangeListener replicaChangeListener;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean connected;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean exchangeFlag;

    @Getter(AccessLevel.NONE)
    private AtomicBoolean acceptCheckpoint;

    @Getter(AccessLevel.NONE)
    private ReplicationEventBus eventBus;

    public ReplicationTemplate(Config config) {
        this.config = config;
        this.messageFactory = new MessageFactory();
        this.connected = new AtomicBoolean(false);
        this.exchangeFlag = new AtomicBoolean(false);
        this.acceptCheckpoint = new AtomicBoolean(false);
        this.eventBus = new ReplicationEventBus();
    }

    public void connect() {
        this.messageTemplate = new MessageTemplate(config, this);
        this.crdt = createReplicatedDataType();
        this.batchChangeScheduler = new BatchChangeScheduler(this);

        Connect message = messageFactory.createConnect(config, getReplicaId());
        messageTemplate.openConnection();
        messageTemplate.sendMessage(message);
        eventBus.post(new ReplicationEvent(Started));
    }

    public void setConnected() {
        connected.compareAndSet(false, true);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void stopReplication(String reason) {
        batchChangeScheduler.shutdown();
        eventBus.post(new ReplicationEvent(Stopped));
        connected.compareAndSet(true, false);
        exchangeFlag.compareAndSet(true, false);
        messageTemplate.closeConnection(reason);
        eventBus.close();
    }

    public void disconnect() {
        Disconnect message = messageFactory.createDisconnect(config, getReplicaId());
        messageTemplate.sendMessage(message);
        stopReplication("User disconnect");
    }

    public void sendChanges() {
        batchChangeScheduler.schedule();
    }

    public void startFeedExchange() {
        if (replicaChangeListener != null) {
            this.getCollection().unsubscribe(replicaChangeListener);
        }
        this.feedJournal = new FeedJournal(this);
        this.replicaChangeListener = new ReplicaChangeListener(this, messageTemplate);
        this.getCollection().subscribe(replicaChangeListener);
        this.exchangeFlag.compareAndSet(false, true);
    }

    public boolean shouldExchangeFeed() {
        return exchangeFlag.get();
    }

    public String getReplicaId() {
        if (StringUtils.isNullOrEmpty(replicaId)) {
            Attributes attributes = getAttributes();
            if (!attributes.hasKey(Attributes.REPLICA)) {
                attributes.set(REPLICA, UUID.randomUUID().toString());
            }
            replicaId = attributes.get(Attributes.REPLICA);
        }
        return replicaId;
    }

    public void setAcceptCheckpoint() {
        acceptCheckpoint.compareAndSet(false, true);
    }

    public boolean shouldAcceptCheckpoint() {
        return acceptCheckpoint.get();
    }

    @Override
    public NitriteCollection getCollection() {
        return config.getCollection();
    }

    public void subscribe(ReplicationEventListener listener) {
        eventBus.register(listener);
    }

    public void unsubscribe(ReplicationEventListener listener) {
        eventBus.deregister(listener);
    }

    public void postEvent(ReplicationEvent event) {
        eventBus.post(event);
    }
}
