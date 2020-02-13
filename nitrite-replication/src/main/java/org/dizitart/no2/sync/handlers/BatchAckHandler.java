package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.FeedJournal;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchAck;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
public class BatchAckHandler implements MessageHandler<BatchAck> {
    private ReplicationTemplate replicationTemplate;

    public BatchAckHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(BatchAck message) {
        Receipt receipt = message.getReceipt();
        FeedJournal journal = replicationTemplate.getFeedJournal();
        journal.accumulate(receipt);
    }
}
