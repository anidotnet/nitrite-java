package org.dizitart.no2.mapper;

import org.dizitart.no2.Document;
import org.dizitart.no2.plugin.NitritePlugin;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteMapper extends NitritePlugin {
    <T> Document asDocument(T object);

    boolean isValueType(Object object);

    Object convertValue(Object object);
}
