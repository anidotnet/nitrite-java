/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.filters;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.IndexedQueryTemplate;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * An abstract implementation of {@link Filter}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Slf4j
public abstract class BaseFilter implements Filter {
    private Field field;
    private Object value;
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMapper nitriteMapper;

    protected BaseFilter(Field field, Object value) {
        this.field = field;
        this.value = value;
    }

    protected NitriteMapper getNitriteMapper() {
        return nitriteMapper;
    }

    protected IndexedQueryTemplate getIndexedQueryTemplate() {
        return indexedQueryTemplate;
    }

    protected boolean isObjectFilter() {
        return this.nitriteMapper != null;
    }

    protected Set<Comparable> convertValues(Set<Comparable> values) {
        if (isObjectFilter()) {
            Set<Comparable> convertedValues = new HashSet<>();

            for (Comparable comparable : values) {
                if (comparable == null
                        || !getNitriteMapper().isValueType(comparable)) {
                    throw new FilterException(errorMessage("search term " + comparable
                            + " is not a comparable", FE_IN_SEARCH_TERM_NOT_COMPARABLE));
                }

                if (getNitriteMapper().isValueType(comparable)) {
                    Comparable convertValue = (Comparable) getNitriteMapper().convertValue(comparable);
                    convertedValues.add(convertValue);
                }
            }

            return convertedValues;
        }
        return values;
    }

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }

    public Field getField() {
        return field;
    }

    public Object getValue() {
        if (isObjectFilter()) {
            validateSearchTerm(getNitriteMapper(), field, value);
            if (getNitriteMapper().isValueType(value)) {
                value = getNitriteMapper().convertValue(value);
            }
        }
        return value;
    }

    private void validateSearchTerm(NitriteMapper nitriteMapper, Field field, Object value) {
        notNull(field, errorMessage("field cannot be null", VE_SEARCH_TERM_NULL_FIELD));

        if (value != null) {
            if (!nitriteMapper.isValueType(value) && !(value instanceof Comparable)) {
                throw new ValidationException(errorMessage("search term is not comparable " + value,
                        FE_SEARCH_TERM_NOT_COMPARABLE));
            }
        }
    }
}
