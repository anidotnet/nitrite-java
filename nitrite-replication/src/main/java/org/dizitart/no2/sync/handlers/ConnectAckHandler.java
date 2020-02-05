package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.ConnectAck;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ConnectAckHandler implements MessageHandler<ConnectAck> {
    private ReplicationTemplate replica;

    public ConnectAckHandler(ReplicationTemplate replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, ConnectAck message) {
        replica.setConnected();
        replica.startFeedExchange();
        replica.sendChanges();
    }
}
