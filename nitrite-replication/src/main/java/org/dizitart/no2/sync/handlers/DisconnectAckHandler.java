package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.sync.LocalReplica;
import org.dizitart.no2.sync.message.DisconnectAck;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DisconnectAckHandler implements MessageHandler<DisconnectAck> {
    private LocalReplica replica;

    public DisconnectAckHandler(LocalReplica replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(WebSocket webSocket, DisconnectAck message) {
        log.debug("Disconnect is successful");
        Long time = message.getMessageHeader().getTimestamp();
        replica.saveLastSyncTime(time);
    }
}
