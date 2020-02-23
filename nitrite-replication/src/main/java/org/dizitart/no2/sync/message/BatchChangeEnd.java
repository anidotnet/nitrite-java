package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeEnd implements DataGateMessage {
    private MessageHeader header;
    private Long lastSynced;
    private Integer batchSize;
    private Integer debounce;
}
