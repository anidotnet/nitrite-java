package org.dizitart.no2.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import org.dizitart.no2.sync.message.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

/**
 * @author Anindya Chatterjee
 */
public class MockDataGateServer {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> collectionReplicaMap = new HashMap<>();
    private Map<String, String> userReplicaMap = new HashMap<>();

    public void buildAndStartServer(int port, String host) {
        Undertow server = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(getWebSocketHandler())
            .build();
        server.start();
    }

    private PathHandler getWebSocketHandler() {
        return path()
            .addPrefixPath("/datagate", websocket((exchange, channel) -> {
                channel.getReceiveSetter().set(new AbstractReceiveListener() {
                    @Override
                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                        handleMessage(channel, message);
                    }
                });
            }));
    }

    private void handleMessage(WebSocketChannel channel, BufferedTextMessage message) throws JsonProcessingException {
        String data = message.getData();
        if (data.contains(MessageType.Connect.code()) || data.contains(MessageType.Disconnect.code())) {
            Connect connect = objectMapper.readValue(data, Connect.class);
            handleConnect(channel, connect);
        } else if (data.contains(MessageType.BatchChangeStart.code())) {
            BatchChangeStart batchChangeStart = objectMapper.readValue(data, BatchChangeStart.class);
            handleBatchChangeStart(channel, batchChangeStart);
        } else if (data.contains(MessageType.BatchChangeContinue.code())) {
            BatchChangeContinue batchChangeContinue = objectMapper.readValue(data, BatchChangeContinue.class);
            handleBatchChangeContinue(channel, batchChangeContinue);
        } else if (data.contains(MessageType.BatchChangeEnd.code())) {
            BatchChangeEnd batchChangeEnd = objectMapper.readValue(data, BatchChangeEnd.class);
            handleBatchChangeEnd(channel, batchChangeEnd);
        } else if (data.contains(MessageType.Feed.code())) {
            DataGateFeed feed = objectMapper.readValue(data, DataGateFeed.class);
            handleDataGateFeed(channel, feed);
        }
    }

    protected void handleConnect(WebSocketChannel channel, Connect connect) {

    }

    protected void handleDataGateFeed(WebSocketChannel channel, DataGateFeed feed) {

    }

    protected void handleBatchChangeEnd(WebSocketChannel channel, BatchChangeEnd batchChangeEnd) {

    }

    protected void handleBatchChangeContinue(WebSocketChannel channel, BatchChangeContinue batchChangeContinue) {

    }

    protected void handleBatchChangeStart(WebSocketChannel channel, BatchChangeStart batchChangeStart) {

    }
}
