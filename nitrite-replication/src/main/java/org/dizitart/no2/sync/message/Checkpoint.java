package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Checkpoint implements DataGateMessage {
    private MessageHeader header;
}
