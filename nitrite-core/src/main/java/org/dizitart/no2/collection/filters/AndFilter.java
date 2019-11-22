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
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.dizitart.no2.exceptions.ErrorMessage.INVALID_AND_FILTER;

@Getter
@Slf4j
@ToString
class AndFilter extends LogicalFilter {
    private Filter[] filters;

    AndFilter(final Filter... filters) {
        this.filters = filters;
    }

    @Override
    public ReadableStream<NitriteId> apply(final NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> result = new LinkedHashSet<>();
        ExecutorService executorService = ExecutorServiceManager.commonPool();

        try {
            List<Callable<ReadableStream<NitriteId>>> tasks = createTasks(filters, documentMap);

            boolean initialCount = true;
            List<Future<ReadableStream<NitriteId>>> futures = executorService.invokeAll(tasks);
            for (Future<ReadableStream<NitriteId>> future : futures) {
                ReadableStream<NitriteId> nitriteIds = future.get();
                if (initialCount && nitriteIds != null) {
                    result.addAll(nitriteIds.toList());
                    initialCount = false;
                } else if (nitriteIds != null) {
                    if (nitriteIds.isEmpty()) {
                        result.clear();
                    }
                    result.retainAll(nitriteIds.toList());
                }
            }
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(INVALID_AND_FILTER, t);
        }

        return ReadableStream.fromIterable(result);
    }
}
