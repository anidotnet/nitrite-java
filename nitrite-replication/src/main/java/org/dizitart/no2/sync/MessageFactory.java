package org.dizitart.no2.sync;

import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public class MessageFactory {
    public Connect createConnect(ReplicationConfig config, String replicaId) {
        Connect message = new Connect();
        message.setMessageHeader(createHeader(MessageType.Connect, config.getCollection().getName(),
            replicaId, config.getUserName()));
        message.setAuthToken(config.getAuthToken());
        return message;
    }

    public Disconnect createDisconnect(ReplicationConfig config, String replicaId) {
        Disconnect message = new Disconnect();
        message.setMessageHeader(createHeader(MessageType.Disconnect, config.getCollection().getName(),
            replicaId, config.getUserName()));
        return message;
    }

    private MessageHeader createHeader(MessageType messageType, String collectionName,
                                       String replicaId, String userName) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection(collectionName);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(replicaId);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }

    public BatchChangeStart createChangeStart(ReplicationConfig config, String replicaId, String uuid) {
        BatchChangeStart message = new BatchChangeStart();
        message.setMessageHeader(createHeader(MessageType.BatchChangeStart, config.getCollection().getName(),
            replicaId, config.getUserName()));
        message.setUuid(uuid);
        message.setBatchSize(config.getChunkSize());
        message.setDebounce(config.getDebounce());
        return message;
    }
}
