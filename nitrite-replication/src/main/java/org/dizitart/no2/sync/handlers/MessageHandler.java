package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.message.DataGateMessage;

/**
 * @author Anindya Chatterjee
 */
public interface MessageHandler<M extends DataGateMessage> {
    void handleMessage(MessageTemplate messageTemplate, M message) throws Exception;
}
