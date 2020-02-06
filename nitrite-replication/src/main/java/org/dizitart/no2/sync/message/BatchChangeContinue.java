package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeContinue implements DataGateMessage {
    private MessageHeader messageHeader;
    private LastWriteWinState feed;
    private String uuid;
    private Integer batchSize;
    private Integer debounce;
}
