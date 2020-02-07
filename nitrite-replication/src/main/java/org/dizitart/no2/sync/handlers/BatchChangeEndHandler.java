package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.BatchChangeEnd;
import org.dizitart.no2.sync.message.BatchEndAck;

/**
 * @author Anindya Chatterjee
 */
public class BatchChangeEndHandler implements MessageHandler<BatchChangeEnd> {
    private ReplicationTemplate replicationTemplate;

    public BatchChangeEndHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, BatchChangeEnd message) {
        MessageFactory factory = replicationTemplate.getMessageFactory();
        BatchEndAck batchEndAck = factory.createBatchEndAck(replicationTemplate.getConfig(),
            replicationTemplate.getReplicaId());

        messageTemplate.sendMessage(batchEndAck);
        replicationTemplate.setAcceptCheckpoint();
    }
}
