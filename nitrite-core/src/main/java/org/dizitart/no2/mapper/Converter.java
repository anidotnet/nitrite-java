package org.dizitart.no2.mapper;

/**
 * @author Anindya Chatterjee
 */
public interface Converter<S, T> {
    T convert(S source);
}

