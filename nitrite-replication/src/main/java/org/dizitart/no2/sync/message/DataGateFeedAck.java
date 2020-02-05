package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class DataGateFeedAck implements DataGateMessage {
    private MessageHeader messageHeader;
    private Receipt receipt;
}
