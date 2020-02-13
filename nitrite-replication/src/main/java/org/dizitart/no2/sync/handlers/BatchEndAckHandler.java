package org.dizitart.no2.sync.handlers;

import lombok.Getter;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchEndAck;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
@Getter
public class BatchEndAckHandler implements MessageHandler<BatchEndAck>, JournalAware {
    private ReplicationTemplate replicationTemplate;

    public BatchEndAckHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(BatchEndAck message) {
        Receipt finalReceipt = getJournal().getFinalReceipt();
        retryFailed(finalReceipt);
    }
}
