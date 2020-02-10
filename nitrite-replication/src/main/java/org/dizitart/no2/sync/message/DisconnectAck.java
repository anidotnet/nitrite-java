package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class DisconnectAck implements DataGateMessage {
    private MessageHeader header;
    private String uuid;
}
