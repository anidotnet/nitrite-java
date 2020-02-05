package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.DisconnectAck;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DisconnectAckHandler implements MessageHandler<DisconnectAck> {
    private ReplicationTemplate replica;

    public DisconnectAckHandler(ReplicationTemplate replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, DisconnectAck message) {
        log.debug("Disconnect is successful");
        Long time = message.getMessageHeader().getTimestamp();
        replica.saveLastSyncTime(time);
    }
}
