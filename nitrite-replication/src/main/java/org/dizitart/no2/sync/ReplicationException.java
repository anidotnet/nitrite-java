package org.dizitart.no2.sync;

import org.dizitart.no2.exceptions.NitriteException;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicationException extends NitriteException {
    public ReplicationException(String errorMessage) {
        super(errorMessage);
    }

    public ReplicationException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
