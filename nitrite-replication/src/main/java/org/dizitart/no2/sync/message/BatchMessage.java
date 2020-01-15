package org.dizitart.no2.sync.message;

/**
 * @author Anindya Chatterjee.
 */
public interface BatchMessage extends DataGateMessage {
    Integer getBatchSize();
    Integer getDebounce();
}
