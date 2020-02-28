package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ErrorMessage implements DataGateMessage {
    private MessageHeader header;
    private String error;
}
