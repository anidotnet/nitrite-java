package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.collection.meta.Attributes.REPLICA;
import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class LocalOperation implements ReplicationOperation {
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
        this.executorService = ExecutorServiceManager.getThreadPool(1, SYNC_THREAD_NAME);
        this.isConnected = new AtomicBoolean(false);
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

    public void handleInsertEvent(Document document, WebSocket connection) {
        LastWriteWinState state = new LastWriteWinState();
        state.setChanges(Collections.singleton(document));

        executorService.submit(() -> sendChangeMessage(connection, state));
    }

    public void handleRemoveEvent(Document document, WebSocket connection) {
        LastWriteWinState state = new LastWriteWinState();
        NitriteId nitriteId = document.getId();
        Long deleteTime = document.getLastModifiedSinceEpoch();
        crdt.getTombstones().put(nitriteId, deleteTime);
        state.setTombstones(Collections.singletonMap(nitriteId.getIdValue(), deleteTime));

        executorService.submit(() -> sendChangeMessage(connection, state));
    }

    public void sendConnect(WebSocket connection) {
        isConnected.set(true);
        executorService.submit(() -> {
            try {
                Connect connect = new Connect();
                connect.setMessageHeader(createMessageInfo(MessageType.Connect));
                connect.setReplicaId(getReplicaId());
                String message = objectMapper.writeValueAsString(connect);
                connection.send(message);
            } catch (Exception e) {
                log.error("failed to send Connect message for " + getReplicaId(), e);
            }
        });
    }

    public void sendDisconnect(WebSocket connection) {
        isConnected.set(false);
        executorService.submit(() -> {
            try {
                Connect connect = new Connect();
                connect.setMessageHeader(createMessageInfo(MessageType.Disconnect));
                connect.setReplicaId(getReplicaId());
                String message = objectMapper.writeValueAsString(connect);
                connection.send(message);
            } catch (Exception e) {
                log.error("failed to send Disconnect message for " + getReplicaId(), e);
            }
        });
    }

    public void sendLocalChanges(WebSocket connection) {
        executorService.submit(() -> {
            if (!isConnected.get()) return;

            Long lastSyncTime = getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            try {
                String initMessage = createChangeStart(uuid);
                connection.send(initMessage);
            } catch (Exception e) {
                log.error("Error while sending BatchChangeStart for " + getReplicaId(), e);
            }

            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                boolean hasMore = true;
                int start = 0;

                @Override
                public void run() {
                    LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, config.getChunkSize());
                    if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        try {
                            String message = createChangeContinue(uuid, state);
                            connection.send(message);
                        } catch (Exception e) {
                            log.error("Error while sending BatchChangeContinue for " + getReplicaId(), e);
                        }

                        start = start + config.getChunkSize();
                    }

                    if (!hasMore) {
                        timer.cancel();
                    }
                }
            }, 0, config.getDebounce());

            try {
                String endMessage = createChangeEnd(uuid, lastSyncTime);
                connection.send(endMessage);
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
        messageHeader.setOrigin(getReplicaId());
        messageHeader.setTimestamp(System.currentTimeMillis());
        messageHeader.setUserName(config.getUserName());
        return messageHeader;
    }

    private void sendChangeMessage(WebSocket connection, LastWriteWinState changes) {
        try {
            if (isConnected.get()) {
                String message = createFeedMessage(changes);
                connection.send(message);
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
