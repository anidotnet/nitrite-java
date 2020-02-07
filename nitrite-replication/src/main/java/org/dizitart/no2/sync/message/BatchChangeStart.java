package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeStart implements DataGateMessage, ReceiptAware {
    private MessageHeader messageHeader;
    private String uuid;
    private Integer batchSize;
    private Integer debounce;
    private LastWriteWinState feed;
}
