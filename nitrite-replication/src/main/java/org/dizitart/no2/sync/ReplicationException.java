package org.dizitart.no2.sync;

import org.dizitart.no2.exceptions.NitriteException;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicationException extends NitriteException {
    private boolean fatal;

    public ReplicationException(String errorMessage, boolean fatal) {
        super(errorMessage);
        this.fatal = fatal;
    }

    public ReplicationException(String errorMessage, Throwable cause, boolean fatal) {
        super(errorMessage, cause);
        this.fatal = fatal;
    }

    public boolean isFatal() {
        return fatal;
    }
}
