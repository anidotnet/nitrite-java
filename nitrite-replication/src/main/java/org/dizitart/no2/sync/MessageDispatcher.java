package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.handlers.ConnectAckHandler;
import org.dizitart.no2.sync.handlers.MessageHandler;
import org.dizitart.no2.sync.message.ConnectAck;
import org.dizitart.no2.sync.message.DataGateMessage;

import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee
 */
public class MessageDispatcher {
    private ReplicationConfig config;
    private NitriteCollection collection;
    private LastWriteWinMap crdt;
    private String replicaId;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    public MessageDispatcher(ReplicationConfig config, NitriteCollection collection,
                             LastWriteWinMap crdt, String replicaId, ObjectMapper objectMapper) {
        this.config = config;
        this.collection = collection;
        this.crdt = crdt;
        this.replicaId = replicaId;
        this.objectMapper = objectMapper;
        this.executorService = ExecutorServiceManager.getThreadPool(1, SYNC_THREAD_NAME);
    }

    public <M extends DataGateMessage> void dispatch(WebSocket webSocket, M message) {
        MessageHandler<M> handler = findHandler(message);
        if (handler != null) {
            executorService.submit(() -> handler.handleMessage(webSocket, message));
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends DataGateMessage> MessageHandler<M> findHandler(DataGateMessage message) {
        if (message instanceof ConnectAck) {
            return (MessageHandler<M>) new ConnectAckHandler(config, collection, crdt, replicaId, objectMapper);
        }
        return null;
    }
}
