package org.dizitart.no2.store;

import org.dizitart.no2.plugin.NitritePlugin;

/**
 * @author Anindya Chatterjee.
 */
public interface StoreConfig extends NitritePlugin {
    String getFilePath();
    boolean isReadOnly();
}
