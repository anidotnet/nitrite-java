package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class RemoteOperation implements ReplicationOperation {
    private LastWriteWinMap crdt;
    private NitriteCollection collection;
    private MessageTransformer messageTransformer;
    private String replicaId;

    public RemoteOperation(Config config, String replicaId) {
        this.replicaId = replicaId;
        this.collection = config.getCollection();
        this.crdt = createReplicatedDataType();
        this.messageTransformer = new MessageTransformer(config.getObjectMapper());
    }

    public void handleMessage(WebSocket webSocket, String text) {
        DataGateMessage message = messageTransformer.transform(text);
        validateMessage(message);
        if (replicaId.equals(message.getMessageHeader().getOrigin())) {
            // ignore broadcast message
            log.debug("Ignoring same origin message for {} - {}", replicaId, text);
            return;
        }


        switch (message.getMessageHeader().getMessageType()) {
            case BatchChangeStart:
                handleBatchChangeStart((BatchChangeStart) message);
                break;
            case BatchChangeContinue:
                handleBatchChangeContinue((BatchChangeContinue) message);
                break;
            case BatchChangeEnd:
                handleBatchChangeEnd((BatchChangeEnd) message);
                break;
            case Feed:
                handleFeed((DataGateFeed) message);
                break;
            case Error:
                handleError((ErrorMessage) message);
                break;
            case Ack:
                handleAck((FeedAck) message);
                break;
        }
    }


    private void validateMessage(DataGateMessage message) {
        if (message == null) {
            throw new ReplicationException("a null message received for " + replicaId);
        } else if (message.getMessageHeader() == null) {
            throw new ReplicationException("invalid message info received for " + replicaId);
        } else if (StringUtils.isNullOrEmpty(message.getMessageHeader().getCollection())) {
            throw new ReplicationException("invalid message info received for " + replicaId);
        } else if (message.getMessageHeader().getMessageType() == null) {
            throw new ReplicationException("invalid message type received for " + replicaId);
        }
    }

    private void handleBatchChangeStart(BatchChangeStart message) {
        // ignore
    }

    private void handleBatchChangeContinue(BatchChangeContinue message) {
        LastWriteWinState state = message.getFeed();
        crdt.merge(state);
    }

    private void handleBatchChangeEnd(BatchChangeEnd message) {
        Long lastSyncTime = message.getLastSynced();
        saveLastSyncTime(lastSyncTime);
    }

    private void handleFeed(DataGateFeed message) {
        LastWriteWinState state = message.getFeed();
        crdt.merge(state);

        Long lastSyncTime = message.getMessageHeader().getTimestamp();
        saveLastSyncTime(lastSyncTime);
    }

    private void handleAck(FeedAck message) {
        Long lastSyncTime = message.getMessageHeader().getTimestamp();
        saveLastSyncTime(lastSyncTime);
    }

    private void handleError(ErrorMessage message) {
        log.error("Error received from server for {} - {}", replicaId, message.getError());
        throw new ServerError(message.getError(), message.getIsFatal());
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }
}
