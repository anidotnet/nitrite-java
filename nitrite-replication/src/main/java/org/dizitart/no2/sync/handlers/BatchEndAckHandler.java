package org.dizitart.no2.sync.handlers;

import lombok.Getter;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.BatchEndAck;
import org.dizitart.no2.sync.message.DataGateFeed;
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
    public void handleMessage(MessageTemplate messageTemplate, BatchEndAck message) {
        Receipt finalReceipt = getJournal().getFinalReceipt();

        if (shouldRetry(finalReceipt)) {
            LastWriteWinState state = createState(finalReceipt);

            MessageFactory factory = replicationTemplate.getMessageFactory();
            DataGateFeed feedMessage = factory.createFeedMessage(replicationTemplate.getConfig(),
                replicationTemplate.getReplicaId(), state);
            messageTemplate.sendMessage(feedMessage);
        }
    }
}
