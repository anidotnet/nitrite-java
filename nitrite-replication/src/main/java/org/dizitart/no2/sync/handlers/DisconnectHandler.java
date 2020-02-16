package org.dizitart.no2.sync.handlers;

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
    public void handleMessage(Disconnect message) {
        replicationTemplate.stopReplication("Server disconnect");
    }
}