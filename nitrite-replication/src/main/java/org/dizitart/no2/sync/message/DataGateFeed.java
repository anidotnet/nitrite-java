package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class DataGateFeed implements DataGateMessage, ReceiptAware {
    private MessageHeader header;
    private LastWriteWinState feed;
}
