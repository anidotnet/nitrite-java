package org.dizitart.no2.sync;

import org.dizitart.no2.sync.message.Connect;
import org.dizitart.no2.sync.message.MessageHeader;
import org.dizitart.no2.sync.message.MessageType;

/**
 * @author Anindya Chatterjee
 */
public class MessageFactory {
    public Connect createConnect(ReplicationConfig config, String replicaId) {
        Connect connect = new Connect();
        connect.setMessageHeader(createHeader(MessageType.Connect, config.getCollection().getName(),
            replicaId, config.getUserName()));
        connect.setAuthToken(config.getAuthToken());
        return connect;
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
}
