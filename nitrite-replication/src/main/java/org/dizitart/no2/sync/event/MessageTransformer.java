package org.dizitart.no2.sync.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public class MessageTransformer {

    public DataGateMessage transform(ObjectMapper objectMapper, String message) {
        try {
            if (isBatchChangeStart(message)) {
                return objectMapper.readValue(message, BatchChangeStart.class);
            } else if (isBatchChangeContinue(message)) {
                return objectMapper.readValue(message, BatchChangeContinue.class);
            } else if (isBatchChangeEnd(message)) {
                return objectMapper.readValue(message, BatchChangeEnd.class);
            } else if (isFeed(message)) {
                return objectMapper.readValue(message, DataGateFeed.class);
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
}
