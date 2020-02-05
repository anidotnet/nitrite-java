package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.ErrorMessage;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ErrorHandler implements MessageHandler<ErrorMessage> {
    private ReplicationTemplate replica;

    public ErrorHandler(ReplicationTemplate replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(MessageTemplate messageTemplate, ErrorMessage message) {
        log.error("Received error message from server {}", message.getError());
        if (message.getIsFatal()) {
            replica.stopReplication(message.getError());
        }
    }
}
