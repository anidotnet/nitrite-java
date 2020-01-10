package org.dizitart.no2.sync.event;

import lombok.Data;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class ReplicationEvent {
    private EventType eventType;
    private LastWriteWinState<?> eventInfo;
}
