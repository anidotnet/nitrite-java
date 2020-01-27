package org.dizitart.no2.test.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
@ServerEndpoint(value="/datagate/{user}/{collection}", configurator = SimpleDataGateServerConfig.class)
public class SimpleDataGateEndpoint {
    private ObjectMapper objectMapper;
    private Repository repository;

    public SimpleDataGateEndpoint() {
        objectMapper = new ObjectMapper();
        repository = Repository.getInstance();
    }

    @OnOpen
    public void onOpen(@PathParam("user") String user,
                       @PathParam("collection") String collection,
                       Session session,
                       EndpointConfig config) throws IOException {
        String error = (String) config.getUserProperties().get("error");
        String replicaId = (String) config.getUserProperties().get("Replica");
        log.info("New request from {}", replicaId);

        if (StringUtils.isNullOrEmpty(error)) {
            log.info("DataGate server connection established with {}", replicaId);
            session.getUserProperties().put("collection", user + "@" + collection);
            session.getUserProperties().put("replica", replicaId);
        } else {
            log.error("Error while establishing connection from {} - {}", replicaId, error);
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessageHeader(createMessageInfo(MessageType.Error, collection, user, repository.getServerId()));
            errorMessage.setError(error);
            String message = objectMapper.writeValueAsString(errorMessage);
            session.getAsyncRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.warn("DataGate server closed due to {}", reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            log.info("Message received at server {}", message);
            if (message.contains(MessageType.Connect.code()) || message.contains(MessageType.Disconnect.code())) {
                Connect connect = objectMapper.readValue(message, Connect.class);
                handleConnect(session, connect);
            } else if (message.contains(MessageType.BatchChangeStart.code())) {
                BatchChangeStart batchChangeStart = objectMapper.readValue(message, BatchChangeStart.class);
                handleBatchChangeStart(session, batchChangeStart);
            } else if (message.contains(MessageType.BatchChangeContinue.code())) {
                BatchChangeContinue batchChangeContinue = objectMapper.readValue(message, BatchChangeContinue.class);
                handleBatchChangeContinue(session, batchChangeContinue);
            } else if (message.contains(MessageType.BatchChangeEnd.code())) {
                BatchChangeEnd batchChangeEnd = objectMapper.readValue(message, BatchChangeEnd.class);
                handleBatchChangeEnd(session, batchChangeEnd);
            } else if (message.contains(MessageType.Feed.code())) {
                DataGateFeed feed = objectMapper.readValue(message, DataGateFeed.class);
                handleDataGateFeed(session, feed);
            }
        } catch (Exception e) {
            log.error("Error while handling message {}", message, e);
        }
    }

    @OnError
    public void onError(Throwable ex) {
        log.error("Error in DataGate server", ex);
    }

    protected void handleConnect(Session channel, Connect connect) {
        String replicaId = connect.getReplicaId();
        String userName = connect.getMessageHeader().getUserName();
        String collection = userName + "@" + connect.getMessageHeader().getCollection();

        if (connect.getMessageHeader().getMessageType() == MessageType.Connect) {
            if (repository.getCollectionReplicaMap().containsKey(collection)) {
                List<String> replicas = repository.getCollectionReplicaMap().get(collection);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                repository.getCollectionReplicaMap().put(collection, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                repository.getCollectionReplicaMap().put(collection, replicas);
            }

            if (repository.getUserReplicaMap().containsKey(userName)) {
                List<String> replicas = repository.getUserReplicaMap().get(userName);
                if (!replicas.contains(replicaId)) {
                    replicas.add(replicaId);
                }
                repository.getUserReplicaMap().put(userName, replicas);
            } else {
                List<String> replicas = new ArrayList<>();
                replicas.add(replicaId);
                repository.getUserReplicaMap().put(userName, replicas);
            }

            if (!repository.getReplicaStore().containsKey(collection)) {
                LastWriteWinMap replica = createCrdt(collection);
                repository.getReplicaStore().put(collection, replica);
            }
        } else if (connect.getMessageHeader().getMessageType() == MessageType.Disconnect) {
            repository.getCollectionReplicaMap().get(collection).remove(replicaId);
            repository.getUserReplicaMap().get(userName).remove(replicaId);
        }
    }

    protected void handleDataGateFeed(Session channel, DataGateFeed feed) {
        String userName = feed.getMessageHeader().getUserName();
        String collection = userName + "@" + feed.getMessageHeader().getCollection();

        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        replica.merge(feed.getFeed());

        try {
            String message = objectMapper.writeValueAsString(feed);
            broadcast(channel, collection, message);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e);
        }
    }

    private void broadcast(Session channel, String collection, String message) {
        channel.getOpenSessions().stream()
            .filter(s -> collection.equals(s.getUserProperties().get("collection")))
            .forEach(s -> s.getAsyncRemote().sendText(message));
    }

    protected void handleBatchChangeEnd(Session channel, BatchChangeEnd batchChangeEnd) {
        Long lastSync = batchChangeEnd.getLastSynced();
        Integer batchSize = batchChangeEnd.getBatchSize();
        Integer debounce = batchChangeEnd.getDebounce();
        String userName = batchChangeEnd.getMessageHeader().getUserName();
        String collection = userName + "@" + batchChangeEnd.getMessageHeader().getCollection();

        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        sendChanges(batchChangeEnd.getMessageHeader().getCollection(), userName, lastSync,
            batchSize, debounce, replica, channel, repository.getServerId());
    }

    protected void handleBatchChangeContinue(Session channel, BatchChangeContinue batchChangeContinue) {
        DataGateFeed feed = new DataGateFeed();

        String userName = batchChangeContinue.getMessageHeader().getUserName();
        String collection = userName + "@" + batchChangeContinue.getMessageHeader().getCollection();
        String replicaId = batchChangeContinue.getMessageHeader().getOrigin();
        LastWriteWinMap replica = repository.getReplicaStore().get(collection);
        replica.merge(batchChangeContinue.getFeed());

        feed.setMessageHeader(createMessageInfo(MessageType.Feed, collection, userName, replicaId));
        feed.setFeed(batchChangeContinue.getFeed());

        try {
            String message = objectMapper.writeValueAsString(feed);
            broadcast(channel, collection, message);
        } catch (Exception e) {
            throw new ReplicationException("failed to broadcast DataGateFeed", e);
        }
    }

    protected void handleBatchChangeStart(Session channel, BatchChangeStart batchChangeStart) {
        log.debug("BatchChangeStart message received " + batchChangeStart);
    }

    private LastWriteWinMap createCrdt(String collection) {
        NitriteCollection nc = repository.getDb().getCollection(collection);
        NitriteMap<NitriteId, Long> nitriteMap =
            repository.getDb().getConfig().getNitriteStore().openMap(collection + "-replica");
        return new LastWriteWinMap(nc, nitriteMap);
    }

    private void sendChanges(String collection, String userName,
                             Long lastSyncTime, Integer chunkSize,
                             Integer debounce, LastWriteWinMap crdt,
                             Session channel, String replicaId) {
        try {
            String uuid = UUID.randomUUID().toString();

            try {
                String initMessage = createChangeStart(uuid, collection, userName,
                    replicaId, chunkSize, debounce);
                log.info("Sending BatchChangeStart message {} from server to {}", initMessage, replicaId);
                channel.getBasicRemote().sendText(initMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeStart to " + replicaId, e);
            }

            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                boolean hasMore = true;
                int start = 0;

                @Override
                public void run() {
                    LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, chunkSize);
                    if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        try {
                            String message = createChangeContinue(uuid, state, collection, userName,
                                replicaId, chunkSize, debounce);
                            log.info("Sending BatchChangeContinue message {} from server to {}", message, replicaId);
                            channel.getBasicRemote().sendText(message);
                        } catch (Exception e) {
                            log.error("Error while sending BatchChangeContinue for " + replicaId, e);
                        }

                        start = start + chunkSize;
                    }
                }
            }, 0, debounce);

            try {
                String endMessage = createChangeEnd(uuid, lastSyncTime, collection, userName,
                    replicaId, chunkSize, debounce);
                log.info("Sending BatchChangeEnd message {} from server to {}", endMessage, replicaId);
                channel.getBasicRemote().sendText(endMessage);
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
                                            String userName, String origin) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection(userName + "@" + collection);
        messageHeader.setMessageType(messageType);
        messageHeader.setOrigin(origin);
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(userName);
        return messageHeader;
    }
}
