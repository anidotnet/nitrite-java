package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LocalChangeContinue implements DataGateMessage {
    private MessageInfo messageInfo;
    private LastWriteWinState state;
    private String uuid;
}
