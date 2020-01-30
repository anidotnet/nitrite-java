package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class FeedAck implements DataGateMessage {
    private MessageHeader messageHeader;
}
