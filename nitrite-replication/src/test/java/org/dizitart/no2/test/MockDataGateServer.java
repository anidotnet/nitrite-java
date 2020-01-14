package org.dizitart.no2.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.message.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MockDataGateServer {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> collectionReplicaMap = new HashMap<>();
    private Map<String, String> userReplicaMap = new HashMap<>();
    private Undertow undertow;

    public void buildAndStartServer(int port, String host) {
        undertow = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(getWebSocketHandler())
            .build();
        undertow.start();
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
        log.info("Raw message " + data);

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
        log.info("Connect message received " + connect);
    }

    protected void handleDataGateFeed(WebSocketChannel channel, DataGateFeed feed) {
        log.info("DataGateFeed message received " + feed);
    }

    protected void handleBatchChangeEnd(WebSocketChannel channel, BatchChangeEnd batchChangeEnd) {
        log.info("BatchChangeEnd message received " + batchChangeEnd);
    }

    protected void handleBatchChangeContinue(WebSocketChannel channel, BatchChangeContinue batchChangeContinue) {
        log.info("BatchChangeContinue message received " + batchChangeContinue);
    }

    protected void handleBatchChangeStart(WebSocketChannel channel, BatchChangeStart batchChangeStart) {
        log.info("BatchChangeStart message received " + batchChangeStart);
    }
}
