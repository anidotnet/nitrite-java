package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class DataGateCheckpoint implements DataGateMessage {
    private MessageType messageType;
    private Long timestamp;
}
