package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.dizitart.no2.sync.LocalReplica;
import org.dizitart.no2.sync.message.ErrorMessage;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ErrorHandler implements MessageHandler<ErrorMessage> {
    private LocalReplica replica;

    public ErrorHandler(LocalReplica replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(WebSocket webSocket, ErrorMessage message) {
        log.error("Received error message from server {}", message.getError());
        if (message.getIsFatal()) {
            replica.close();
        }
    }
}
