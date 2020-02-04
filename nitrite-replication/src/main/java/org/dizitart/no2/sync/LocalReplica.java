package org.dizitart.no2.sync;

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
public class LocalReplica implements ReplicationOperation {
    private ReplicationConfig config;
    private MessageFactory messageFactory;
    private MessageTemplate messageTemplate;
    private AtomicBoolean connectedIndicator;
    private LastWriteWinMap crdt;
    private ChangeManager changeManager;
    private String replicaId;

    public LocalReplica(ReplicationConfig config) {
        this.config = config;
        this.messageFactory = new MessageFactory();
    }

    public void connect() {
        this.messageTemplate = new MessageTemplate(config, this);
        this.connectedIndicator = new AtomicBoolean(false);
        this.crdt = createReplicatedDataType();
        this.changeManager = new ChangeManager(this);

        Connect message = messageFactory.createConnect(config, getReplicaId());
        messageTemplate.openConnection();
        messageTemplate.sendMessage(message);
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

    public void close(String reason) {
        // release resources
        changeManager.shutdown();
        connectedIndicator.compareAndSet(true, false);
        messageTemplate.closeConnection(reason);
    }

    public void disconnect() {
        Disconnect message = messageFactory.createDisconnect(config, getReplicaId());
        messageTemplate.sendMessage(message);
        close("User disconnect");
    }

    public boolean isConnected() {
        return connectedIndicator.get();
    }
}
