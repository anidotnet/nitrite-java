package org.dizitart.no2.sync.handlers;

import lombok.Getter;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateFeed;
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
    public void handleMessage(MessageTemplate messageTemplate, DataGateFeedAck message) {
        Receipt receipt = message.getReceipt();

        Receipt finalReceipt = getJournal().accumulate(receipt);
        if (shouldRetry(finalReceipt)) {
            LastWriteWinState state = createState(finalReceipt);

            MessageFactory factory = replicationTemplate.getMessageFactory();
            DataGateFeed feedMessage = factory.createFeedMessage(replicationTemplate.getConfig(),
                replicationTemplate.getReplicaId(), state);
            messageTemplate.sendMessage(feedMessage);
        }

        if (replicationTemplate.shouldAcceptCheckpoint()) {
            Long time = message.getHeader().getTimestamp();
            replicationTemplate.saveLastSyncTime(time);
        }
    }
}
