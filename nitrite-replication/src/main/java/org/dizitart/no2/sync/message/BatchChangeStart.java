package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeStart implements ReceiptAware {
    private MessageHeader header;
    private Integer batchSize;
    private Integer debounce;
    private LastWriteWinState feed;
}
