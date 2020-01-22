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
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.*;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
public class MockDataGateServer {
    private ObjectMapper objectMapper;
    private Map<String, List<String>> collectionReplicaMap;
    private Map<String, List<String>> userReplicaMap;
    private Map<String, LastWriteWinMap> replicaStore;
    private Undertow undertow;
    private Nitrite db;
//    private ExecutorService executorService;
    private String serverId;

    public MockDataGateServer(int port, String host) {
        objectMapper = new ObjectMapper();
        collectionReplicaMap = new HashMap<>();
        userReplicaMap = new HashMap<>();
        replicaStore = new HashMap<>();

        db = NitriteBuilder.get().openOrCreate();
        serverId = UUID.randomUUID().toString();
        undertow = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(getWebSocketHandler())
            .build();
        undertow.start();
    }

    public void stop() {
        undertow.stop();
        db.close();
    }

    protected void handleConnect(WebSocketChannel channel, Connect connect) {
        log.debug("Connect message received " + connect);
        String replicaId = connect.getReplicaId();
        String userName = connect.getMessageHeader().getUserName();
        String collection = userName + "@" + connect.getMessageHeader().getCollection();

        if (connect.getMessageHeader().getMessageType() == MessageType.Connect) {
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
        } else if (connect.getMessageHeader().getMessageType() == MessageType.Disconnect) {
            collectionReplicaMap.get(collection).remove(replicaId);
            userReplicaMap.get(userName).remove(replicaId);
        }
    }

    protected void handleDataGateFeed(WebSocketChannel channel, DataGateFeed feed) {
        log.debug("DataGateFeed message received " + feed);
        String userName = feed.getMessageHeader().getUserName();
        String collection = userName + "@" + feed.getMessageHeader().getCollection();

        LastWriteWinMap replica = replicaStore.get(collection);
        replica.merge(feed.getFeed());

        try {
            String message = objectMapper.writeValueAsString(feed);
            broadcast(message, channel);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e);
        }
    }

    protected void handleBatchChangeEnd(WebSocketChannel channel, BatchChangeEnd batchChangeEnd) {
        log.debug("BatchChangeEnd message received " + batchChangeEnd);
        Long lastSync = batchChangeEnd.getLastSynced();
        Integer batchSize = batchChangeEnd.getBatchSize();
        Integer debounce = batchChangeEnd.getDebounce();
        String userName = batchChangeEnd.getMessageHeader().getUserName();
        String collection = userName + "@" + batchChangeEnd.getMessageHeader().getCollection();

        LastWriteWinMap replica = replicaStore.get(collection);
        sendChanges(batchChangeEnd.getMessageHeader().getCollection(), userName, lastSync,
            batchSize, debounce, replica, channel, serverId);
    }

    protected void handleBatchChangeContinue(WebSocketChannel channel, BatchChangeContinue batchChangeContinue) {
        log.debug("BatchChangeContinue message received " + batchChangeContinue);
        DataGateFeed feed =  new DataGateFeed();

        String userName = batchChangeContinue.getMessageHeader().getUserName();
        String collection = userName + "@" + batchChangeContinue.getMessageHeader().getCollection();
        String replicaId = batchChangeContinue.getMessageHeader().getSource();
        LastWriteWinMap replica = replicaStore.get(collection);
        replica.merge(batchChangeContinue.getFeed());

        feed.setMessageHeader(createMessageInfo(MessageType.Feed, collection, userName, replicaId));
        feed.setFeed(batchChangeContinue.getFeed());

        try {
            String message = objectMapper.writeValueAsString(feed);
            broadcast(message, channel);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e);
        }
    }

    protected void handleBatchChangeStart(WebSocketChannel channel, BatchChangeStart batchChangeStart) {
        log.debug("BatchChangeStart message received " + batchChangeStart);
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

    private void handleMessage(WebSocketChannel channel, BufferedTextMessage message) throws JsonProcessingException {
        String data = message.getData();
        log.info("Message received - " + data);
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

    private LastWriteWinMap createCrdt(String collection) {
        NitriteCollection nc = db.getCollection(collection);
        NitriteMap<NitriteId, Long> nitriteMap =
            db.getConfig().getNitriteStore().openMap(collection + "-replica");
        return new LastWriteWinMap(nc, nitriteMap);
    }

    private void broadcast(String message, WebSocketChannel channel) {
        for (WebSocketChannel ch : channel.getPeerConnections()) {
            if (ch != channel) {
                WebSockets.sendText(message, ch, null);
            }
        }
    }

    private void sendChanges(String collection, String userName,
                             Long lastSyncTime, Integer chunkSize,
                             Integer debounce, LastWriteWinMap crdt,
                             WebSocketChannel channel, String replicaId) {
        try {
            String uuid = UUID.randomUUID().toString();
            int start = 0;
            boolean hasMore = true;

            try {
                String initMessage = createChangeStart(uuid, collection, userName,
                    replicaId, chunkSize, debounce);
                System.out.println("server-side start " + initMessage);
                WebSockets.sendText(initMessage, channel, null);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeStart to " + replicaId, e);
            }

            while (hasMore) {
                LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, chunkSize);
                if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                    hasMore = false;
                }

                if (hasMore) {
                    try {
                        String message = createChangeContinue(uuid, state, collection, userName,
                            replicaId, chunkSize, debounce);
                        System.out.println("server-side continue " + message);
                        WebSockets.sendText(message, channel, null);
                    } catch (Exception e) {
                        log.error("Error while sending BatchChangeContinue for " + replicaId, e);
                    }

                    try {
                        Thread.sleep(debounce);
                    } catch (InterruptedException e) {
                        log.error("thread interrupted", e);
                    }

                    start = start + chunkSize;
                }
            }

            try {
                String endMessage = createChangeEnd(uuid, lastSyncTime, collection, userName,
                    replicaId, chunkSize, debounce);
                System.out.println("server-side end " + endMessage + " with peers " + channel.getPeerConnections().size());
                WebSockets.sendText(endMessage, channel, null);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeEnd for " + replicaId, e);
            }
        } catch (Exception e) {
            throw new ReplicationException("failed to send local changes message for " + replicaId, e);
        }
    }

    private String createChangeStart(String uuid,
                                     String collection, String userName, String replicaId,
                                     Integer chunkSize, Integer debounce) {
        try {
            BatchChangeStart message = new BatchChangeStart();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeStart,
                collection, userName, replicaId));
            message.setUuid(uuid);
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeStart message", e);
        }
    }

    private String createChangeContinue(String uuid, LastWriteWinState state,
                                        String collection, String userName, String replicaId,
                                        Integer chunkSize, Integer debounce) {
        try {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeContinue,
                collection, userName, replicaId));
            message.setFeed(state);
            message.setUuid(uuid);
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeContinue message", e);
        }
    }

    private String createChangeEnd(String uuid, Long lastSyncTime,
                                   String collection, String userName, String replicaId,
                                   Integer chunkSize, Integer debounce) {
        try {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeEnd,
                collection, userName, replicaId));
            message.setUuid(uuid);
            message.setLastSynced(lastSyncTime);
            message.setBatchSize(chunkSize);
            message.setDebounce(debounce);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeEnd message", e);
        }
    }

    private MessageHeader createMessageInfo(MessageType messageType, String collection,
                                            String userName, String replicaId) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection(collection);
        messageHeader.setMessageType(messageType);
        messageHeader.setSource(replicaId);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }
}
