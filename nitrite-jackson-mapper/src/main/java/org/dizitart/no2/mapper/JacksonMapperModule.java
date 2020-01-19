package org.dizitart.no2.mapper;

import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.module.NitritePlugin;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class JacksonMapperModule implements NitriteModule {
    private JacksonMapper jacksonMapper;

    public JacksonMapperModule() {
        jacksonMapper = new JacksonMapper();
    }

    public JacksonMapperModule(JacksonModule... jacksonModules) {
        jacksonMapper = new JacksonMapper(jacksonModules);
    }

    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(jacksonMapper);
    }
}
