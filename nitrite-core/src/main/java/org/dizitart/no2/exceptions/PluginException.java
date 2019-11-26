package org.dizitart.no2.exceptions;

/**
 * @author Anindya Chatterjee.
 */
public class PluginException extends NitriteException {
    public PluginException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    public PluginException(ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
