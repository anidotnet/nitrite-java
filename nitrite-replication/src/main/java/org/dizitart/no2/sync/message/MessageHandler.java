package org.dizitart.no2.sync.message;

/**
 * @author Anindya Chatterjee
 */
public interface MessageHandler<T extends DataGateMessage> {
    void handleMessage(T message);
}
