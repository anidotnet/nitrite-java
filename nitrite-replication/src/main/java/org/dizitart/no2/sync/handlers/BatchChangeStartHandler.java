package org.dizitart.no2.sync.handlers;

import lombok.Data;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchAck;
import org.dizitart.no2.sync.message.BatchChangeStart;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeStartHandler implements MessageHandler<BatchChangeStart>, ReceiptAckSender<BatchAck> {
    private ReplicationTemplate replicationTemplate;

    public BatchChangeStartHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, BatchChangeStart message) {
        sendAck(messageTemplate, message);
    }

    @Override
    public BatchAck createAck(Receipt receipt) {
        MessageFactory factory = replicationTemplate.getMessageFactory();
        return factory.createBatchAck(replicationTemplate.getConfig(),
            replicationTemplate.getReplicaId(), receipt);
    }
}
