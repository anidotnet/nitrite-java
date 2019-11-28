/**
 * @author Anindya Chatterjee
 */

@NitritePluginContainer(plugins = {
    NitriteMVStore.class,
    MVStoreConfig.class
})
package org.dizitart.no2;

import org.dizitart.no2.plugin.NitritePluginContainer;
import org.dizitart.no2.store.MVStoreConfig;
import org.dizitart.no2.store.NitriteMVStore;