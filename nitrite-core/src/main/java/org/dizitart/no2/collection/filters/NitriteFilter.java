package org.dizitart.no2.collection.filters;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public abstract class NitriteFilter implements Filter {
    private NitriteConfig nitriteConfig;
    private String collectionName;
    private Boolean objectFilter = false;
}
