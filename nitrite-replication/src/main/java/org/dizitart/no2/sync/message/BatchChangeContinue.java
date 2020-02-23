package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeContinue implements ReceiptAware {
    private MessageHeader header;
    private LastWriteWinState feed;
    private Integer batchSize;
    private Integer debounce;
}
