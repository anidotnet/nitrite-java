package org.dizitart.no2.mapper;

import org.dizitart.no2.module.NitritePlugin;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteMapper extends NitritePlugin {
    <Source, Target> Target convert(Source source, Class<Target> type);

    boolean isValueType(Class<?> type);

    boolean isValue(Object object);
}
