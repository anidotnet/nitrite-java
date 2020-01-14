package org.dizitart.no2.sync.connection;

/**
 * @author Anindya Chatterjee
 */
public interface Connection extends AutoCloseable {
    void open();
    void connect();
    void sendMessage(String message);
    void sendAndReceive();
}
