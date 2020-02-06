package org.dizitart.no2.sync;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.message.Connect;
import org.dizitart.no2.sync.message.Disconnect;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;

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

    public ReplicationTemplate(Config config) {
        this.config = config;
        this.messageFactory = new MessageFactory();
        this.connected = new AtomicBoolean(false);
        this.exchangeFlag = new AtomicBoolean(false);
    }

    public void connect() {
        this.messageTemplate = new MessageTemplate(config, this);
        this.crdt = createReplicatedDataType();
        this.batchChangeScheduler = new BatchChangeScheduler(this);

        Connect message = messageFactory.createConnect(config, getReplicaId());
        messageTemplate.openConnection();
        messageTemplate.sendMessage(message);
    }

    public void setConnected() {
        connected.compareAndSet(false, true);
    }

    public void setDisconnected() {
        connected.compareAndSet(true, false);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void stopReplication(String reason) {
        // release resources
        batchChangeScheduler.shutdown();
        connected.compareAndSet(true, false);
        exchangeFlag.compareAndSet(true, false);
        messageTemplate.closeConnection(reason);
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

    @Override
    public NitriteCollection getCollection() {
        return config.getCollection();
    }
}
