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
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.Filter;

import java.util.Map;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.newInstance;
import static org.dizitart.no2.filters.Filter.eq;

/**
 * A utility class for {@link Document}.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class DocumentUtils {

    /**
     * Determines whether a document has recently been updated/created than the other.
     *
     * @param recent the recent document
     * @param older  the older document
     * @return the boolean value
     */
    public static boolean isRecent(Document recent, Document older) {
        if (recent.getRevision() == older.getRevision()) {
            return recent.getLastModifiedTime() >= older.getLastModifiedTime();
        }
        return recent.getRevision() > older.getRevision();
    }

    /**
     * Create unique filter to identify the `document`.
     *
     * @param document the document
     * @return the unique filter
     */
    public static Filter createUniqueFilter(Document document) {
        return eq(DOC_ID, document.getId().getIdValue());
    }

    /**
     * Creates an empty document having all fields of a `type` set to `null`.
     *
     * @param <T>           the type parameter
     * @param nitriteMapper the nitrite mapper
     * @param type          the type
     * @return the document
     */
    public static <T> Document skeletonDocument(NitriteMapper nitriteMapper, Class<T> type) {
        T dummy = newInstance(type, true);
        Document document = nitriteMapper.asDocument(dummy);
        return removeValues(document);
    }

    private Document removeValues(Document document) {
        if (document == null) return null;
        for (KeyValuePair entry : document) {
            if (entry.getValue() instanceof Map) {
                document.put(entry.getKey(), removeValues((Document) entry.getValue()));
            } else {
                document.put(entry.getKey(), null);
            }
        }
        return document;
    }
}
