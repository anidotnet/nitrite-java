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
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@ToString
class NotFilter implements Filter {
    private Filter filter;
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMapper nitriteMapper;

    NotFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        filter.setIndexedQueryTemplate(indexedQueryTemplate);
        filter.setNitriteMapper(nitriteMapper);
        return matchedSet(documentMap);
    }

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> resultSet = new LinkedHashSet<>(documentMap.keySet());
        resultSet.removeAll(filter.apply(documentMap));
        return resultSet;
    }
}
