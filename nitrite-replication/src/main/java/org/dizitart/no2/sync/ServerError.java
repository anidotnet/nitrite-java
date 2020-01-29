package org.dizitart.no2.sync;

import lombok.Getter;

/**
 * @author Anindya Chatterjee
 */
public class ServerError extends ReplicationException {
    @Getter
    private boolean isFatal;

    public ServerError(String errorMessage, boolean isFatal) {
        super(errorMessage);
        this.isFatal = isFatal;
    }

    public ServerError(String errorMessage, Throwable cause, boolean isFatal) {
        super(errorMessage, cause);
        this.isFatal = isFatal;
    }
}
