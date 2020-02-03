package org.dizitart.no2.sync;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.message.Connect;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;

/**
 * @author Anindya Chatterjee
 */
public class LocalReplica implements ReplicationOperation {
    private ReplicationConfig config;
    private MessageFactory messageFactory;
    private MessageTemplate messageTemplate;
    private AtomicBoolean connected;
    private String replicaId;

    public LocalReplica(ReplicationConfig config) {
        this.config = config;
        this.messageFactory = new MessageFactory();
        this.messageTemplate = new MessageTemplate(config, this);
        this.connected = new AtomicBoolean(false);
    }

    public void connect() {
        Connect message = messageFactory.createConnect(config, getReplicaId());
        messageTemplate.openConnection();
        messageTemplate.sendMessage(message);
    }

    public void onConnected() {
        connected.compareAndSet(false, true);
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

    public void onError(Throwable error) {

    }

    public void onClose() {

    }
}
