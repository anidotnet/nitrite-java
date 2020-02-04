package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.sync.LocalReplica;
import org.dizitart.no2.sync.message.ConnectAck;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ConnectAckHandler implements MessageHandler<ConnectAck> {
    private LocalReplica replica;

    public ConnectAckHandler(LocalReplica replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(WebSocket webSocket, ConnectAck message) {
        replica.getConnectedIndicator().compareAndSet(false, true);
        recieveFeed();
        replica.getChangeManager().sendChanges();
    }
}
