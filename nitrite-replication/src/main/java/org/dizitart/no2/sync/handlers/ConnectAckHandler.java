package org.dizitart.no2.sync.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.ReplicationConfig;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.BatchChangeContinue;
import org.dizitart.no2.sync.message.BatchChangeEnd;
import org.dizitart.no2.sync.message.ConnectAck;
import org.dizitart.no2.sync.message.MessageType;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ConnectAckHandler implements MessageHandler<ConnectAck> {
    private ReplicationConfig config;
    private NitriteCollection collection;
    private LastWriteWinMap crdt;
    private String replicaId;
    private ObjectMapper objectMapper;

    public ConnectAckHandler(ReplicationConfig config, NitriteCollection collection,
                             LastWriteWinMap crdt, String replicaId, ObjectMapper objectMapper) {
        this.config = config;
        this.collection = collection;
        this.crdt = crdt;
        this.replicaId = replicaId;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleMessage(WebSocket webSocket, ConnectAck message) {
        Long lastSyncTime = getLastSyncTime();

        Timer timer = new Timer();
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
                        String strMessage = createChangeContinue(message.getUuid(), state);
                        log.info("Sending BatchChangeContinue message from {} - {}", replicaId, strMessage);
                        webSocket.send(strMessage);
                    } catch (Exception e) {
                        log.error("Error while sending BatchChangeContinue from {}", replicaId, e);
                    }

                    start = start + config.getChunkSize();
                }

                if (!hasMore) {
                    timer.cancel();
                }
            }
        }, 0, config.getDebounce());

        try {
            String endMessage = createChangeEnd(message.getUuid(), lastSyncTime);
            log.info("Sending BatchChangeEnd message from {} - {}", replicaId, endMessage);
            webSocket.send(endMessage);
        } catch (Exception e) {
            log.error("Error while sending BatchChangeEnd from {}", replicaId, e);
        }
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }

    private String createChangeEnd(String uuid, Long lastSyncTime) {
        try {
            BatchChangeEnd message = new BatchChangeEnd();
            message.setMessageHeader(createHeader(MessageType.BatchChangeEnd, config.getUserName(), replicaId));
            message.setUuid(uuid);
            message.setLastSynced(lastSyncTime);
            message.setBatchSize(config.getChunkSize());
            message.setDebounce(config.getDebounce());
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeEnd message", e);
        }
    }

    private String createChangeContinue(String uuid, LastWriteWinState state) {
        try {
            BatchChangeContinue message = new BatchChangeContinue();
            message.setMessageHeader(createHeader(MessageType.BatchChangeContinue, config.getUserName(), replicaId));
            message.setFeed(state);
            message.setUuid(uuid);
            message.setBatchSize(config.getChunkSize());
            message.setDebounce(config.getDebounce());
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create BatchChangeContinue message", e);
        }
    }
}
