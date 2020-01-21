package org.dizitart.no2.sync.connection;

import org.dizitart.no2.sync.message.MessageHandler;

/**
 * @author Anindya Chatterjee
 */
public interface Connection extends AutoCloseable {
    void open();
    void sendMessage(String message);

    static Connection create(ConnectionConfig config, MessageHandler messageHandler) {
        if (config instanceof WebSocketConfig) {
            return new WebSocketConnection(config, messageHandler);
        }
        return null;
    }
}
