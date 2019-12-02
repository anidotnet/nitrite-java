package org.dizitart.no2.exceptions;

/**
 * @author Anindya Chatterjee.
 */
public class PluginException extends NitriteException {
    public PluginException(String errorMessage) {
        super(errorMessage);
    }

    public PluginException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
