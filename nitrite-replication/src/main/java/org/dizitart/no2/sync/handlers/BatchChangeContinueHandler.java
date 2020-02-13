package org.dizitart.no2.sync.handlers;

import lombok.Data;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchAck;
import org.dizitart.no2.sync.message.BatchChangeContinue;
import org.dizitart.no2.sync.message.Receipt;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeContinueHandler implements MessageHandler<BatchChangeContinue>, ReceiptAckSender<BatchAck> {
    private ReplicationTemplate replicationTemplate;

    public BatchChangeContinueHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(BatchChangeContinue message) {
        sendAck(message);
    }

    @Override
    public BatchAck createAck(Receipt receipt) {
        MessageFactory factory = replicationTemplate.getMessageFactory();
        return factory.createBatchAck(replicationTemplate.getConfig(),
            replicationTemplate.getReplicaId(), receipt);
    }
}
