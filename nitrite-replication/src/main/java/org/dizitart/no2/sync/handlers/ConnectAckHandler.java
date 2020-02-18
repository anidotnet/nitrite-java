package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
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
    public void handleMessage(ConnectAck message) {
        replica.collectGarbage(message.getTombstoneTtl());
        replica.setConnected();
        replica.startFeedExchange();
        replica.sendChanges();
    }
}
