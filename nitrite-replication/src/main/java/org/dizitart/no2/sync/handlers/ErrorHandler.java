package org.dizitart.no2.sync.handlers;

import okhttp3.WebSocket;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.LocalReplica;
import org.dizitart.no2.sync.ReplicationConfig;
import org.dizitart.no2.sync.message.ErrorMessage;

/**
 * @author Anindya Chatterjee
 */
public class ErrorHandler implements MessageHandler<ErrorMessage> {
    public ErrorHandler(ReplicationConfig config, LocalReplica replica) {

    }

    @Override
    public void handleMessage(WebSocket webSocket, ErrorMessage message) {

    }

    @Override
    public NitriteCollection getCollection() {
        return null;
    }
}
