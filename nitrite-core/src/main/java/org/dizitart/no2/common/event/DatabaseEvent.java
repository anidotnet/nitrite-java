package org.dizitart.no2.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class DatabaseEvent {
    private EventType eventType;
    private NitriteConfig config;

    public enum EventType {
        Opened,
        Commit,
        Closing,
        Closed
    }
}
