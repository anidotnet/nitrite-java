package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.Receipt;
import org.dizitart.no2.sync.message.ReceiptAware;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptAckSender<Ack extends DataGateMessage> {
    ReplicationTemplate getReplicationTemplate();
    Ack createAck(Receipt receipt);

    default void sendAck(ReceiptAware message) {
        if (message != null) {
            LastWriteWinState state = message.getFeed();
            getReplicationTemplate().getCrdt().merge(state);

            Receipt receipt = message.calculateReceipt();
            Ack ack = createAck(receipt);
            MessageTemplate messageTemplate = getReplicationTemplate().getMessageTemplate();
            messageTemplate.sendMessage(ack);
        }
    }
}
