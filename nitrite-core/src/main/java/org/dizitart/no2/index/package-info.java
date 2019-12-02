/**
 * @author Anindya Chatterjee
 */
@NitritePluginContainer(plugins = {
    UniqueIndexer.class,
    NonUniqueIndexer.class
})
package org.dizitart.no2.index;

import org.dizitart.no2.plugin.NitritePluginContainer;