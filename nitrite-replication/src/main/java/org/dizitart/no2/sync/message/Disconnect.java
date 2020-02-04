package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Disconnect implements DataGateMessage {
    private MessageHeader messageHeader;
}