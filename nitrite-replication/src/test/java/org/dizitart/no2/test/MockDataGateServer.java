package org.dizitart.no2.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.message.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
public class MockDataGateServer {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, List<String>> collectionReplicaMap = new HashMap<>();
    private Map<String, List<String>> userReplicaMap = new HashMap<>();
    private Map<String, LastWriteWinMap> replicaStore = new HashMap<>();
    private Undertow undertow;
    private Nitrite db;

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
                Map<String, List<String>> requestHeaders = exchange.getRequestHeaders();
                validateAuth(requestHeaders);

                channel.getReceiveSetter().set(new AbstractReceiveListener() {
                    @Override
                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                        try {
                            handleMessage(channel, message);
                        } catch (Exception e) {
                            log.error("Error while handling message at server", e);
                        }
                    }
                });

                channel.resumeReceives();
            }));
    }

    private void validateAuth(Map<String, List<String>> requestHeaders) {
        if (!requestHeaders.containsKey("Authorization")) {
            throw new SecurityException("connection not authorized");
        }

        String authorization = requestHeaders.get("Authorization").get(0);
        if (authorization.equals("Bearer abcd")) return;

        throw new SecurityException("invalid token");
    }

    public LastWriteWinMap createCrdt(String collection) {
        db = NitriteBuilder.get().openOrCreate();
        NitriteCollection nc = db.getCollection(collection);
        NitriteMap<NitriteId, Long> nitriteMap =
            db.getConfig().getNitriteStore().openMap(collection + "-replica");
        return new LastWriteWinMap(nc, nitriteMap);
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
        log.info("Connect message received " + connect);
        String replicaId = connect.getReplicaId();
        String userName = connect.getMessageInfo().getUserName();
        String collection = userName + "@" + connect.getMessageInfo().getCollection();

        if (connect.getMessageInfo().getMessageType() == MessageType.Connect) {
            if (collectionReplicaMap.containsKey(collection)) {
                List<String> replicas = collectionReplicaMap.get(collection);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                collectionReplicaMap.put(collection, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                collectionReplicaMap.put(collection, replicas);
            }

            if (userReplicaMap.containsKey(userName)) {
                List<String> replicas = userReplicaMap.get(userName);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                userReplicaMap.put(userName, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                userReplicaMap.put(userName, replicas);
            }

            if (!replicaStore.containsKey(collection)) {
                LastWriteWinMap replica = createCrdt(collection);
                replicaStore.put(collection, replica);
            }
        } else if (connect.getMessageInfo().getMessageType() == MessageType.Disconnect) {
            collectionReplicaMap.get(collection).remove(replicaId);
            userReplicaMap.get(userName).remove(replicaId);
        }
    }

    protected void handleDataGateFeed(WebSocketChannel channel, DataGateFeed feed) {
        log.info("DataGateFeed message received " + feed);
        String userName = feed.getMessageInfo().getUserName();
        String collection = userName + "@" + feed.getMessageInfo().getCollection();

        LastWriteWinMap replica = replicaStore.get(collection);
        replica.merge(feed.getChanges());


    }

    protected void handleBatchChangeEnd(WebSocketChannel channel, BatchChangeEnd batchChangeEnd) {
//        log.info("BatchChangeEnd message received " + batchChangeEnd);
    }

    protected void handleBatchChangeContinue(WebSocketChannel channel, BatchChangeContinue batchChangeContinue) {
//        log.info("BatchChangeContinue message received " + batchChangeContinue);
    }

    protected void handleBatchChangeStart(WebSocketChannel channel, BatchChangeStart batchChangeStart) {
//        log.info("BatchChangeStart message received " + batchChangeStart);
    }

    public void stop() {
        undertow.stop();
    }

    private void broadcast(String message, WebSocketChannel channel) {
        for (WebSocketChannel ch : channel.getPeerConnections()) {
            if (ch != channel) {
                WebSockets.sendText(message, ch, null);
            }
        }
    }
}
