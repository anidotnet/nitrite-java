package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.Module;

import java.util.List;

/**
 * @author Anindya Chatterjee
 */
public interface JacksonModule {
    List<Class<?>> getDataTypes();
    Module getModule();
}
