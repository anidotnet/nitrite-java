package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public class MessageTransformer {
    private ObjectMapper objectMapper;

    public MessageTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DataGateMessage transform(String message) {
        try {
            if (isError(message)) {
                return objectMapper.readValue(message, ErrorMessage.class);
            } else if (isConnect(message)) {
                return objectMapper.readValue(message, Connect.class);
            } else if (isConnectAck(message)) {
                return objectMapper.readValue(message, ConnectAck.class);
            } else if (isDisconnect(message)) {
                return objectMapper.readValue(message, Disconnect.class);
            } else if (isDisconnectAck(message)) {
                return objectMapper.readValue(message, DisconnectAck.class);
            } else if (isBatchChangeStart(message)) {
                return objectMapper.readValue(message, BatchChangeStart.class);
            } else if (isBatchChangeContinue(message)) {
                return objectMapper.readValue(message, BatchChangeContinue.class);
            } else if (isBatchChangeEnd(message)) {
                return objectMapper.readValue(message, BatchChangeEnd.class);
            } else if (isBatchAck(message)) {
                return objectMapper.readValue(message, BatchAck.class);
            } else if (isBatchEndAck(message)) {
                return objectMapper.readValue(message, BatchEndAck.class);
            } else if (isFeed(message)) {
                return objectMapper.readValue(message, DataGateFeed.class);
            } else if (isFeedAck(message)) {
                return objectMapper.readValue(message, DataGateFeedAck.class);
            }
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to transform message from server", e, true);
        }
        return null;
    }

    private boolean isError(String message) {
        return message.contains(MessageType.Error.code());
    }

    private boolean isConnect(String message) {
        return message.contains(MessageType.Connect.code());
    }

    private boolean isConnectAck(String message) {
        return message.contains(MessageType.ConnectAck.code());
    }

    private boolean isDisconnect(String message) {
        return message.contains(MessageType.Disconnect.code());
    }

    private boolean isDisconnectAck(String message) {
        return message.contains(MessageType.DisconnectAck.code());
    }

    private boolean isBatchChangeStart(String message) {
        return message.contains(MessageType.BatchChangeStart.code());
    }

    private boolean isBatchChangeContinue(String message) {
        return message.contains(MessageType.BatchChangeContinue.code());
    }

    private boolean isBatchChangeEnd(String message) {
        return message.contains(MessageType.BatchChangeEnd.code());
    }

    private boolean isBatchAck(String message) {
        return message.contains(MessageType.BatchAck.code());
    }

    private boolean isBatchEndAck(String message) {
        return message.contains(MessageType.BatchEndAck.code());
    }

    private boolean isFeed(String message) {
        return message.contains(MessageType.DataGateFeed.code());
    }

    private boolean isFeedAck(String message) {
        return message.contains(MessageType.DataGateFeedAck.code());
    }
}
