package org.dizitart.no2.store.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventInfo {
    private StoreEvents event;
    private NitriteConfig nitriteConfig;
}
