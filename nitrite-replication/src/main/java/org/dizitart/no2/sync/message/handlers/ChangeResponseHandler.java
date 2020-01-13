package org.dizitart.no2.sync.message.handlers;

import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.message.ChangeResponse;
import org.dizitart.no2.sync.message.MessageHandler;

/**
 * @author Anindya Chatterjee
 */
public class ChangeResponseHandler implements MessageHandler<ChangeResponse> {
    private Replica replica;

    public ChangeResponseHandler(Replica replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(Connection connection, ChangeResponse message) {

    }
}
