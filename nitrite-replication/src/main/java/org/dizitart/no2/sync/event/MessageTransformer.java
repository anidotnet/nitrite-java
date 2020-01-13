package org.dizitart.no2.sync.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee
 */
public class MessageTransformer {
    private ObjectMapper objectMapper;

    public MessageTransformer() {
        this.objectMapper = new ObjectMapper();
    }

    public DataGateMessage transform(String message) {
        try {
            if (isChangeResponse(message)) {
                return objectMapper.readValue(message, ChangeResponse.class);
            } else if (isFeed(message)) {
                return objectMapper.readValue(message, DataGateFeed.class);
            } else if (isCheckpoint(message)) {
                return objectMapper.readValue(message, DataGateCheckpoint.class);
            }
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to transform message from server", e);
        }
        return null;
    }

    private boolean isChangeResponse(String message) {
        return message.contains(MessageType.ChangeResponse.code());
    }

    private boolean isFeed(String message) {
        return message.contains(MessageType.Feed.code());
    }

    private boolean isCheckpoint(String message) {
        return message.contains(MessageType.Checkpoint.code());
    }
}
