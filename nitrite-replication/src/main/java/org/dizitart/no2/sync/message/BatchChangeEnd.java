package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeEnd implements DataGateMessage {
    private MessageInfo messageInfo;
    private String uuid;
}
