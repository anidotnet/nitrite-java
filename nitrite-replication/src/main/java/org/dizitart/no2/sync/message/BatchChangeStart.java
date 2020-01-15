package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeStart implements BatchMessage {
    private MessageInfo messageInfo;
    private String uuid;
    private Integer batchSize;
    private Integer debounce;
}
