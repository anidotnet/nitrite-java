package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;

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
    private AtomicBoolean isConnected;

    public LocalOperation(ReplicationConfig config) {
        this.config = config;
        this.collection = config.getCollection();
        this.crdt = createReplicatedDataType();
        this.objectMapper = config.getObjectMapper();
        this.executorService = ExecutorServiceManager.syncExecutor();
        this.isConnected = new AtomicBoolean(false);
    }

    @Override
    public ReplicationConfig getConfig() {
        return config;
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }

    public String getReplicaId() {
        Attributes attributes = getAttributes();
        if (!attributes.hasKey(Attributes.REPLICA)) {
            attributes.set(REPLICA, UUID.randomUUID().toString());
        }
        return attributes.get(Attributes.REPLICA);
    }

    public void handleInsertEvent(Document document) {
        executorService.submit(() -> {
            LastWriteWinState state = new LastWriteWinState();
            state.setChanges(Collections.singleton(document));
            sendChangeMessage(state);
        });
    }

    public void handleRemoveEvent(Document document) {
        executorService.submit(() -> {
            LastWriteWinState state = new LastWriteWinState();
            NitriteId nitriteId = document.getId();
            Long deleteTime = document.getLastModifiedSinceEpoch();
            crdt.getTombstones().put(nitriteId, deleteTime);

            state.setTombstones(Collections.singletonMap(nitriteId.getIdValue(), deleteTime));
            sendChangeMessage(state);
        });
    }

    public void sendConnect() {
        isConnected.set(true);
        executorService.submit(() -> {
            try {
                Connect connect = new Connect();
                connect.setMessageHeader(createMessageInfo(MessageType.Connect));
                connect.setReplicaId(getReplicaId());
                String message = objectMapper.writeValueAsString(connect);
                getConnection().sendMessage(message);
            } catch (Exception e) {
                log.error("failed to send Connect message for " + getReplicaId(), e);
            }
        });
    }

    public void sendDisconnect() {
        isConnected.set(false);
        executorService.submit(() -> {
            try {
                Connect connect = new Connect();
                connect.setMessageHeader(createMessageInfo(MessageType.Disconnect));
                connect.setReplicaId(getReplicaId());
                String message = objectMapper.writeValueAsString(connect);
                getConnection().sendMessage(message);
            } catch (Exception e) {
                log.error("failed to send Disconnect message for " + getReplicaId(), e);
            }
        });
    }

    public void sendLocalChanges() {
        executorService.submit(() -> {
            if (!isConnected.get()) return;

            int start = 0;
            boolean hasMore = true;
            Long lastSyncTime = getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            try {
                String initMessage = createChangeStart(uuid);
                getConnection().sendMessage(initMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeStart for " + getReplicaId(), e);
            }

            while (hasMore) {
                LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, config.getChunkSize());
                if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                    hasMore = false;
                }

                if (hasMore) {
                    try {
                        String message = createChangeContinue(uuid, state);
                        getConnection().sendMessage(message);
                    } catch (Exception e) {
                        log.error("Error while sending BatchChangeContinue for " + getReplicaId(), e);
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
                String endMessage = createChangeEnd(uuid, lastSyncTime);
                getConnection().sendMessage(endMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeEnd for " + getReplicaId(), e);
            }
        });
    }

    private String createChangeStart(String uuid) {
        try {
            BatchChangeStart message = new BatchChangeStart();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeStart));
            message.setUuid(uuid);
            message.setBatchSize(config.getChunkSize());
            message.setDebounce(config.getDebounce());
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeStart message", e);
        }
    }

    private String createChangeContinue(String uuid, LastWriteWinState state) {
        try {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeContinue));
            message.setFeed(state);
            message.setUuid(uuid);
            message.setBatchSize(config.getChunkSize());
            message.setDebounce(config.getDebounce());
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeContinue message", e);
        }
    }

    private String createChangeEnd(String uuid, Long lastSyncTime) {
        try {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setMessageHeader(createMessageInfo(MessageType.BatchChangeEnd));
            message.setUuid(uuid);
            message.setLastSynced(lastSyncTime);
            message.setBatchSize(config.getChunkSize());
            message.setDebounce(config.getDebounce());
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeEnd message", e);
        }
    }

    private MessageHeader createMessageInfo(MessageType messageType) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCollection(collection.getName());
        messageHeader.setMessageType(messageType);
        messageHeader.setSource(getReplicaId());
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(config.getUserName());
        return messageHeader;
    }

    private void sendChangeMessage(LastWriteWinState changes) {
        try {
            if (isConnected.get()) {
                String message = createFeedMessage(changes);
                getConnection().sendMessage(message);
                saveLastSyncTime();
            }
        } catch (Exception e) {
            log.error("Error while sending DataGateFeed for " + getReplicaId(), e);
        }
    }

    private String createFeedMessage(LastWriteWinState state) {
        try {
            DataGateFeed feed = new DataGateFeed();
            feed.setMessageHeader(createMessageInfo(MessageType.Feed));
            feed.setFeed(state);
            return objectMapper.writeValueAsString(feed);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create DataGateFeed message", e);
        }
    }
}
