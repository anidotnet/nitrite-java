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
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.collection.index.ComparableIndexer;
import org.dizitart.no2.collection.index.IndexType;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQUAL_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQ_NOT_SPATIAL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

@Getter
@ToString
class EqualsFilter extends BaseFilter {

    EqualsFilter(Field field, Object value) {
        super(field, value);
    }

    @Override
    public ReadableStream<NitriteId> apply(NitriteMap<NitriteId, Document> nitriteMap) {
        Object value = getValue();

        if (getField().getName().equals(DOC_ID)) {
            Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
            NitriteId nitriteId = null;
            if (value instanceof Long) {
                nitriteId = NitriteId.createId((Long) value);
            }

            if (nitriteId != null) {
                if (nitriteMap.containsKey(nitriteId)) {
                    nitriteIdSet.add(nitriteId);
                }
            }
            return ReadableStream.fromIterable(nitriteIdSet);
        } else if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField())
                && value != null) {

            String indexType = getIndexedQueryTemplate().findIndex(getField()).getIndexType();
            Indexer indexer = getIndexedQueryTemplate().getIndexer(indexType);

            if (Objects.equals(indexType, IndexType.Fulltext)) {
                Set<NitriteId> nitriteIds = matchedSet(nitriteMap);
                return ReadableStream.fromIterable(nitriteIds);
            }

            if (value instanceof Comparable && indexer instanceof ComparableIndexer) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) indexer;
                return comparableIndexer.findEqual(nitriteMap, getField(), (Comparable) value);
            } else {
                throw new FilterException(errorMessage(value + " is not comparable",
                        FE_EQUAL_NOT_COMPARABLE));
            }
        } else {
            return matchedSet(nitriteMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        Object value = getValue();

        for (KeyValuePair<NitriteId, Document> entry: documentMap.entries()) {
            Document document = entry.getValue();
            Object fieldValue = document.get(getField().getName());
            if (deepEquals(fieldValue, value)) {
                nitriteIdSet.add(entry.getKey());
            }
        }
        return nitriteIdSet;
    }
}
