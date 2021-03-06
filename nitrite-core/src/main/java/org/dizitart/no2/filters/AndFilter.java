/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;

import java.util.Arrays;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
class AndFilter extends LogicalFilter {
    private final Filter rhs;
    private final Filter lhs;

    AndFilter(Filter rhs, Filter lhs) {
        this.rhs = rhs;
        this.lhs = lhs;
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        return rhs.apply(element) && lhs.apply(element);
    }

    @Override
    public List<Filter> getFilters() {
        return Arrays.asList(rhs, lhs);
    }
}
