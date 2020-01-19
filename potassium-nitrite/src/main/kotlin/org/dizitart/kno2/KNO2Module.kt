package org.dizitart.kno2

import org.dizitart.no2.mapper.JacksonModule
import org.dizitart.no2.module.NitriteModule
import org.dizitart.no2.module.NitritePlugin
import org.dizitart.no2.spatial.SpatialIndexer

/**
 *
 * @author Anindya Chatterjee
 */
open class KNO2Module(private vararg val modules: JacksonModule) : NitriteModule {

    override fun plugins(): MutableSet<NitritePlugin> {
        return setOf(KNO2JacksonMapper(*modules), SpatialIndexer())
    }
}