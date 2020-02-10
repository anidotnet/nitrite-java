package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchAck implements DataGateMessage {
    private MessageHeader header;
    private Receipt receipt;
}
