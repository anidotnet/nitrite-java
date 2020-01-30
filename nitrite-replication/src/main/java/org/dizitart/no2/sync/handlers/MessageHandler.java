package org.dizitart.no2.sync.handlers;

import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.MessageHeader;
import org.dizitart.no2.sync.message.MessageType;

import static org.dizitart.no2.collection.meta.Attributes.LAST_SYNCED;

/**
 * @author Anindya Chatterjee
 */
public interface MessageHandler<M extends DataGateMessage> {
    void handleMessage(WebSocket webSocket, M message);
    NitriteCollection getCollection();

    default MessageHeader createHeader(MessageType messageType, String userName,
                                       String replicaId) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection(getCollection().getName());
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(replicaId);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }

    default Attributes getAttributes() {
        Attributes attributes = getCollection().getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
            saveAttributes(attributes);
        }
        return attributes;
    }

    default void saveAttributes(Attributes attributes) {
        getCollection().setAttributes(attributes);
    }

    default Long getLastSyncTime() {
        Attributes attributes = getAttributes();
        String syncTimeStr = attributes.get(LAST_SYNCED);
        if (StringUtils.isNullOrEmpty(syncTimeStr)) {
            return Long.MIN_VALUE;
        } else {
            return Long.parseLong(syncTimeStr);
        }
    }
}
