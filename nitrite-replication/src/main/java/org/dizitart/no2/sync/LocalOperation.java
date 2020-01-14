package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class LocalOperation implements ConnectionAware, ReplicationOperation {
    private ReplicationConfig config;
    private NitriteCollection collection;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;
    private LastWriteWinMap crdt;
    private String replicaId;

    public LocalOperation(String replicaId, ReplicationConfig config) {
        this.replicaId = replicaId;
        this.config = config;
        this.collection = config.getCollection();
        this.crdt = createReplicatedDataType();
        this.objectMapper = config.getObjectMapper();
        this.executorService = ExecutorServiceManager.syncExecutor();
    }

    @Override
    public ReplicationConfig getConfig() {
        return config;
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }

    public void handleInsertEvent(Document document) {
        LastWriteWinState state = new LastWriteWinState();
        state.setChanges(Collections.singleton(document));
        sendChangeMessage(state);
    }

    public void handleRemoveEvent(Document document) {
        LastWriteWinState state = new LastWriteWinState();
        NitriteId nitriteId = document.getId();
        Long deleteTime = document.getLastModifiedSinceEpoch();
        state.setTombstones(Collections.singletonMap(nitriteId, deleteTime));
        sendChangeMessage(state);
    }

    public void sendConnect() {
        try {
            getConnection().connect();
            Connect connect = new Connect();
            connect.setMessageInfo(createMessageInfo(MessageType.Connect));
            connect.setReplicaId(replicaId);
            String message = objectMapper.writeValueAsString(connect);
            getConnection().sendMessage(message);
        } catch (Exception e) {
            throw new ReplicationException("failed to send Connect message for " + replicaId, e);
        }
    }

    public void sendDisconnect() {
        try {
            Connect connect = new Connect();
            connect.setMessageInfo(createMessageInfo(MessageType.Disconnect));
            connect.setReplicaId(replicaId);
            String message = objectMapper.writeValueAsString(connect);
            getConnection().sendMessage(message);
        } catch (Exception e) {
            throw new ReplicationException("failed to send Disconnect message for " + replicaId, e);
        }
    }

    public void sendLocalChanges() {
        try {
            Long lastSyncTime = getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            executorService.submit(() -> {
                int start = 0;
                boolean hasMore = true;

                try {
                    String initMessage = createChangeStart(uuid);
                    getConnection().sendMessage(initMessage);
                } catch (Exception e) {
                    log.error("Error while sending BatchChangeStart for " + replicaId, e);
                }

                while (hasMore) {
                    LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, config.getChunkSize());
                    if (state.getChanges().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        try {
                            String message = createChangeContinue(uuid, state);
                            getConnection().sendMessage(message);
                        } catch (Exception e) {
                            log.error("Error while sending BatchChangeContinue for " + replicaId, e);
                        }

                        try {
                            Thread.sleep(config.getDebounce());
                        } catch (InterruptedException e) {
                            log.error("thread interrupted", e);
                        }

                        start = start + config.getChunkSize();
                    }
                }

                try {
                    String endMessage = createChangeEnd(uuid);
                    getConnection().sendMessage(endMessage);
                } catch (Exception e) {
                    log.error("Error while sending BatchChangeEnd for " + replicaId, e);
                }
            });
        } catch (Exception e) {
            throw new ReplicationException("failed to send local changes message for " + replicaId, e);
        }
    }

    private String createFeedMessage(LastWriteWinState state) {
        try {
            DataGateFeed feed = new DataGateFeed();
            feed.setMessageInfo(createMessageInfo(MessageType.Feed));
            feed.setChanges(state);
            return objectMapper.writeValueAsString(feed);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create DataGateFeed message", e);
        }
    }

    private String createChangeStart(String uuid) {
        try {
            BatchChangeStart message = new BatchChangeStart();
            message.setMessageInfo(createMessageInfo(MessageType.BatchChangeStart));
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeStart message", e);
        }
    }

    private String createChangeContinue(String uuid, LastWriteWinState state) {
        try {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setMessageInfo(createMessageInfo(MessageType.BatchChangeContinue));
            message.setChanges(state);
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeContinue message", e);
        }
    }

    private String createChangeEnd(String uuid) {
        try {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setMessageInfo(createMessageInfo(MessageType.BatchChangeEnd));
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeEnd message", e);
        }
    }

    private MessageInfo createMessageInfo(MessageType messageType) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setCollection(collection.getName());
        messageInfo.setMessageType(messageType);
        messageInfo.setServer(config.getConnectionConfig().getUrl());
        messageInfo.setTimestamp(System.currentTimeMillis());
        messageInfo.setUserName(config.getUserName());
        messageInfo.setReplicaId(replicaId);
        return messageInfo;
    }

    private void sendChangeMessage(LastWriteWinState changes) {
        try {
            String message = createFeedMessage(changes);
            getConnection().sendMessage(message);
            saveLastSyncTime();
        } catch (Exception e) {
            log.error("Error while sending DataGateFeed for " + replicaId, e);
            throw new ReplicationException("failed to send DataGateFeed message for " + replicaId, e);
        }
    }
}
