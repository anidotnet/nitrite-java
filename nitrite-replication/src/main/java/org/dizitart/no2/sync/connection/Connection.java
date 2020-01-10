package org.dizitart.no2.sync.connection;

/**
 * @author Anindya Chatterjee
 */
public interface Connection extends AutoCloseable {
    void open();

    void sendMessage(String message);
}
