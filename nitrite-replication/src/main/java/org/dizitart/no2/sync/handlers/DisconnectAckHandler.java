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
    private ReplicationTemplate replicationTemplate;

    public DisconnectAckHandler(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, DisconnectAck message) {
        log.debug("Disconnect is successful");
        Long time = message.getHeader().getTimestamp();
        replicationTemplate.saveLastSyncTime(time);
    }
}
