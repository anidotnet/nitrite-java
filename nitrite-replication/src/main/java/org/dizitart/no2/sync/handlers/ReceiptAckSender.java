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

    default void sendAck(MessageTemplate messageTemplate, ReceiptAware message) {
        if (message != null) {
            LastWriteWinState state = message.getFeed();
            getReplicationTemplate().getCrdt().merge(state);

            Receipt receipt = message.getReceipt();
            Ack ack = createAck(receipt);
            messageTemplate.sendMessage(ack);
        }
    }
}
