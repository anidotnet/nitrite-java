package org.dizitart.no2.mapper;

import org.dizitart.no2.plugin.NitritePlugin;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteMapper extends NitritePlugin {
    <Source, Target> Target convertType(Source source, Class<Target> targetClass);

    boolean isValueType(Class<?> type);

    boolean isValue(Object object);
}
