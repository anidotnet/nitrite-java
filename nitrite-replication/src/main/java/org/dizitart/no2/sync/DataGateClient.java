package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.message.DataGateCheckpoint;
import org.dizitart.no2.sync.message.DataGateFeed;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.MessageType;

/**
 * @author Anindya Chatterjee.
 */
public class DataGateClient extends WebSocketAdapter {
    private ObjectMapper objectMapper;
    private Connection connection;

    public DataGateClient(Connection connection) {
        this.objectMapper = new ObjectMapper();
        this.connection = connection;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        if (text.contains(MessageType.Checkpoint.name())) {
            DataGateCheckpoint checkpoint = objectMapper.readValue(text, DataGateCheckpoint.class);
            processCheckpoint(checkpoint);
        } else if (text.contains(MessageType.Feed.name())) {
            DataGateFeed dataGateFeed = objectMapper.readValue(text, DataGateFeed.class);
            processFeed(dataGateFeed);
        }
    }

    public void sendTextMessage(DataGateMessage dataGateMessage) {
        try {
            String message = objectMapper.writeValueAsString(dataGateMessage);
            connection.sendMessage(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to send the message to server", e);
        }
    }
}
