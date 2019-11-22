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

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.util.ValidationUtils;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

@Getter
@ToString
class InFilter extends BaseFilter {
    private Set<Comparable> comparableSet;

    InFilter(String field, Comparable... values) {
        super(field, values);
        this.comparableSet = new HashSet<>();
        Collections.addAll(this.comparableSet, values);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateInFilterValue(getField(), comparableSet);

        this.comparableSet = convertValues(this.comparableSet);

        if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField()) && comparableSet != null) {
            ComparableIndexer comparableIndexer = getIndexedQueryTemplate().getComparableIndexer();
            return comparableIndexer.findIn(getField(), comparableSet);
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = document.get(getField());

            if (fieldValue instanceof Comparable) {
                Comparable comparable = (Comparable) fieldValue;
                if (comparableSet.contains(comparable)) {
                    nitriteIdSet.add(entry.getKey());
                }
            }
        }
        return nitriteIdSet;
    }

    private void validateInFilterValue(String field, Collection<Comparable> values) {
        ValidationUtils.notNull(field, errorMessage("field cannot be null", VE_IN_FILTER_NULL_FIELD));
        ValidationUtils.notEmpty(field, errorMessage("field cannot be empty", VE_IN_FILTER_EMPTY_FIELD));
        ValidationUtils.notNull(values, errorMessage("values cannot be null", VE_IN_FILTER_NULL_VALUES));
        if (values.size() == 0) {
            throw new ValidationException(errorMessage("values cannot be empty", VE_IN_FILTER_EMPTY_VALUES));
        }
    }
}
