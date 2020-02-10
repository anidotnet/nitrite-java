package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.Disconnect;

/**
 * @author Anindya Chatterjee
 */
public class DisconnectHandler implements MessageHandler<Disconnect> {
    private ReplicationTemplate replicationTemplate;

    public DisconnectHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, Disconnect message) throws Exception {
        Long time = message.getHeader().getTimestamp();
        replicationTemplate.saveLastSyncTime(time);
        replicationTemplate.stopReplication("Server disconnect");
    }
}
