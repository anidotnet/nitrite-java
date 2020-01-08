package org.dizitart.no2.sync;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinRegister;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class ReplicationEvent {
    private EventType eventType;
    private LastWriteWinRegister<?> eventInfo;
}
