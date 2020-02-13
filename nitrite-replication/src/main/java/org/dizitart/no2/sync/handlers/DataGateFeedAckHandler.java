package org.dizitart.no2.sync.handlers;

import lombok.Getter;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.DataGateFeedAck;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
@Getter
public class DataGateFeedAckHandler implements MessageHandler<DataGateFeedAck>, JournalAware {
    private ReplicationTemplate replicationTemplate;

    public DataGateFeedAckHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(DataGateFeedAck message) {
        Receipt receipt = message.getReceipt();

        Receipt finalReceipt = getJournal().accumulate(receipt);
        retryFailed(finalReceipt);

        if (replicationTemplate.shouldAcceptCheckpoint()) {
            Long time = message.getHeader().getTimestamp();
            replicationTemplate.saveLastSyncTime(time);
        }
    }
}
