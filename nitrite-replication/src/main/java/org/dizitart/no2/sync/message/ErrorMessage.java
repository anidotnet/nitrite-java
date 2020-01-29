package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ErrorMessage implements DataGateMessage {
    private MessageHeader messageHeader;
    private String error;
    private Boolean isFatal;
}
