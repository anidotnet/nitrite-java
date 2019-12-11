package org.dizitart.no2.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.collection.Document;

/**
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeConverter<Source> {
    private Class<Source> sourceType;
    private Converter<Source, Document> sourceConverter;
    private Converter<Document, Source> targetConverter;
}
