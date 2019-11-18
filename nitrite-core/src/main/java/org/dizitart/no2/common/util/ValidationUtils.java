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

package org.dizitart.no2.common.util;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.ErrorMessage;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * A validation utility class.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class ValidationUtils {
    /**
     * Validates if a string is empty or `null`.
     *
     * @param value   the string value
     * @param message the error message
     */
    public static void notEmpty(String value, ErrorMessage message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if a {@link CharSequence} is empty or `null`.
     *
     * @param value   the value
     * @param message the message
     */
    public static void notEmpty(CharSequence value, ErrorMessage message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if an object is `null`.
     *
     * @param value   the object
     * @param message the message
     */
    public static void notNull(Object value, ErrorMessage message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if an array contains `null` item.
     *
     * @param array   the array to check for `null` object
     * @param message the message
     * */
    public static <T> void containsNull(T[] array, ErrorMessage message) {
        for (T element : array) {
            if (element == null) {
                throw new ValidationException(message);
            }
        }
    }

    /**
     * Validates if a field of a document can be indexed.
     *
     * @param fieldValue the field value
     * @param field      the field
     */
    public static void validateDocumentIndexField(Object fieldValue, String field) {
        if (fieldValue instanceof Document) {
            throw new InvalidOperationException(errorMessage(
                    "compound index on field " + field + " is not supported",
                    IOE_COMPOUND_INDEX));
        }

        if (fieldValue instanceof Iterable || fieldValue.getClass().isArray()) {
            throw new IndexingException(errorMessage("indexing on arrays or collections " +
                    "are not supported for field " + field, IE_INDEX_ON_ARRAY_NOT_SUPPORTED));
        }

        if (!(fieldValue instanceof Comparable)) {
            throw new IndexingException(errorMessage("cannot index on non comparable field " + field,
                    IE_INDEX_ON_NON_COMPARABLE_FIELD));
        }
    }

}
