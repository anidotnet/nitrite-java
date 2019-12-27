package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Anindya Chatterjee
 */
public abstract class SimpleNitriteModule extends SimpleModule {
    public abstract Class<?> getDataType();
}
