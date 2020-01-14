package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.message.BatchChangeContinue;
import org.dizitart.no2.sync.message.BatchChangeEnd;
import org.dizitart.no2.sync.message.BatchChangeStart;
import org.dizitart.no2.sync.message.DataGateFeed;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class RemoteOperation implements ConnectionAware, ReplicationOperation {
    private ReplicationConfig config;
    private LastWriteWinMap crdt;
    private NitriteCollection collection;

    public RemoteOperation(ReplicationConfig config) {
        this.config = config;
        this.collection = config.getCollection();
        this.crdt = createReplicatedDataType();
    }

    @Override
    public ReplicationConfig getConfig() {
        return config;
    }

    public void handleReplicationEvent(ReplicationEvent event) {
        switch (event.getMessage().getMessageInfo().getMessageType()) {
            case BatchChangeStart:
                handleBatchChangeStart((BatchChangeStart) event.getMessage());
                break;
            case BatchChangeContinue:
                handleBatchChangeContinue((BatchChangeContinue) event.getMessage());
                break;
            case BatchChangeEnd:
                handleBatchChangeEnd((BatchChangeEnd) event.getMessage());
                break;
            case Feed:
                handleFeed((DataGateFeed) event.getMessage());
                break;
        }
    }

    private void handleBatchChangeStart(BatchChangeStart message) {
        // ignore
    }

    private void handleBatchChangeContinue(BatchChangeContinue message) {
        LastWriteWinState state = message.getChanges();
        crdt.merge(state);
    }

    private void handleBatchChangeEnd(BatchChangeEnd message) {
        saveLastSyncTime();
    }

    private void handleFeed(DataGateFeed message) {
        LastWriteWinState state = message.getChanges();
        crdt.merge(state);
        saveLastSyncTime();
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }
}
