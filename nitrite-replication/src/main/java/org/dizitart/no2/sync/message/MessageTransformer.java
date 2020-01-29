package org.dizitart.no2.sync.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.sync.ReplicationException;

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
            if (isBatchChangeStart(message)) {
                return objectMapper.readValue(message, BatchChangeStart.class);
            } else if (isBatchChangeContinue(message)) {
                return objectMapper.readValue(message, BatchChangeContinue.class);
            } else if (isBatchChangeEnd(message)) {
                return objectMapper.readValue(message, BatchChangeEnd.class);
            } else if (isFeed(message)) {
                return objectMapper.readValue(message, DataGateFeed.class);
            } else if (isError(message)) {
                return objectMapper.readValue(message, ErrorMessage.class);
            } else if (isAck(message)) {
                return objectMapper.readValue(message, DataGateAck.class);
            }
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to transform message from server", e);
        }
        return null;
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

    private boolean isFeed(String message) {
        return message.contains(MessageType.Feed.code());
    }

    private boolean isError(String message) {
        return message.contains(MessageType.Error.code());
    }

    private boolean isAck(String message) {
        return message.contains(MessageType.Ack.code());
    }
}
