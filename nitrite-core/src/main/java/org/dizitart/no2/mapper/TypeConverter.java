package org.dizitart.no2.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Anindya Chatterjee
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TypeConverter<Source, Target> {
    private Class<Source> sourceType;
    private Class<Target> targetType;
    private Converter<Source, Target> sourceConverter;
    private Converter<Target, Source> targetConverter;
}
