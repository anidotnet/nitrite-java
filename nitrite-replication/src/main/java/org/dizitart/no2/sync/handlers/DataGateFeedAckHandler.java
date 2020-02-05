package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.DataGateFeedAck;

/**
 * @author Anindya Chatterjee
 */
public class DataGateFeedAckHandler implements MessageHandler<DataGateFeedAck> {
    private ReplicationTemplate replicationTemplate;

    public DataGateFeedAckHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, DataGateFeedAck message) throws Exception {
        //TODO: create balance sheet and match with receipt.
        // delete entries from balance sheet based on receipt
        // find a way to save balance sheet before exit, so that
        // it can be resumed.
    }
}
