package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BatchChangeContinue implements DataGateMessage, ChangeMessage {
    private MessageInfo messageInfo;
    private LastWriteWinState changes;
    private String uuid;
}
