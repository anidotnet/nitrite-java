package org.dizitart.no2.sync.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class ReplicationEvent {
    private ReplicationEventType eventType;
    private Throwable error;

    public ReplicationEvent(ReplicationEventType eventType) {
        this(eventType, null);
    }
}
