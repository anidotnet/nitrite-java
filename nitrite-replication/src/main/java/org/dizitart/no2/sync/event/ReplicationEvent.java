package org.dizitart.no2.sync.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.sync.message.DataGateMessage;

/**
 * @author Anindya Chatterjee.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplicationEvent {
    private DataGateMessage message;
}
