package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.Checkpoint;

/**
 * @author Anindya Chatterjee
 */
public class CheckpointHandler implements MessageHandler<Checkpoint> {
    private ReplicationTemplate replicationTemplate;

    public CheckpointHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, Checkpoint message) {
        if (message != null) {
            Long timestamp = message.getMessageHeader().getTimestamp();
            replicationTemplate.saveLastSyncTime(timestamp);
        }
    }
}
