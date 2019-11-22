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
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.index.IndexedQueryTemplate;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
abstract class LogicalFilter implements Filter {
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMapper nitriteMapper;

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }

    List<Callable<ReadableStream<NitriteId>>> createTasks(Filter[] filters,
                                               final NitriteMap<NitriteId, Document> documentMap) {
        List<Callable<ReadableStream<NitriteId>>> tasks = new ArrayList<>();
        for (final Filter filter : filters) {
            filter.setIndexedQueryTemplate(indexedQueryTemplate);
            filter.setNitriteMapper(nitriteMapper);
            tasks.add(() -> {
                try {
                    return filter.apply(documentMap);
                } catch (Exception e) {
                    log.error("Error while executing filter " + filter.toString(), e);
                    throw e;
                }
            });
        }
        return tasks;
    }
}
