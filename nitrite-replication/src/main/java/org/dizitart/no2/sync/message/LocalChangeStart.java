package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LocalChangeStart implements DataGateMessage {
    private MessageInfo messageInfo;
    private Integer size;
    private String uuid;
}
