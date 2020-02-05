package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.Receipt;
import org.dizitart.no2.sync.message.DataGateFeed;
import org.dizitart.no2.sync.message.DataGateFeedAck;

/**
 * @author Anindya Chatterjee
 */
public class DataGateFeedHandler implements MessageHandler<DataGateFeed> {
    private ReplicationTemplate replicationTemplate;

    public DataGateFeedHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, DataGateFeed message) {
        if (message != null) {
            LastWriteWinState state = message.getFeed();
            replicationTemplate.getCrdt().merge(state);

            Receipt receipt = message.getReceipt();
            MessageFactory factory = replicationTemplate.getMessageFactory();
            DataGateFeedAck feedAck = factory.createFeedAck(replicationTemplate.getConfig(),
                replicationTemplate.getReplicaId(), receipt);
            messageTemplate.sendMessage(feedAck);
        }
    }
}
