package org.dizitart.no2.sync.message;

import org.dizitart.no2.sync.connection.Connection;

/**
 * @author Anindya Chatterjee
 */
public interface MessageHandler<T extends DataGateMessage> {
    void handleMessage(Connection connection, T message);
}
