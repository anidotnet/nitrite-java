package org.dizitart.no2.sync;

/**
 * @author Anindya Chatterjee
 */
public class ServerError extends ReplicationException {
    public ServerError(String errorMessage) {
        super(errorMessage);
    }

    public ServerError(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
