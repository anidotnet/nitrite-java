package org.dizitart.no2.sync.handlers;

import lombok.Data;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.DataGateFeed;
import org.dizitart.no2.sync.message.DataGateFeedAck;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
@Data
public class DataGateFeedHandler implements MessageHandler<DataGateFeed>, ReceiptAckSender<DataGateFeedAck> {
    private ReplicationTemplate replicationTemplate;

    public DataGateFeedHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, DataGateFeed message) {
        sendAck(messageTemplate, message);
    }

    @Override
    public DataGateFeedAck createAck(Receipt receipt) {
        MessageFactory factory = replicationTemplate.getMessageFactory();
        return factory.createFeedAck(replicationTemplate.getConfig(),
            replicationTemplate.getReplicaId(), receipt);
    }
}
