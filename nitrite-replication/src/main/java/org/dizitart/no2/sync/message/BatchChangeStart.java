package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeStart implements BatchMessage {
    private MessageHeader messageHeader;
    private String uuid;
    private Integer batchSize;
    private Integer debounce;
}
