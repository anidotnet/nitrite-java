/*
 *  Copyright 2017-2019 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection;

import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.NitriteSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public interface Document extends Iterable<KeyValuePair<String, Object>>, Cloneable, NitriteSerializable {

    static Document createDocument() {
        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        return new NitriteDocument(document);
    }

    static Document createDocument(String key, Object value) {
        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put(key, value);
        return new NitriteDocument(document);
    }

    static Document createDocument(Map<String, Object> documentMap) {
        LinkedHashMap<String, Object> document = new LinkedHashMap<>(documentMap);
        return new NitriteDocument(document);
    }

    Document put(final String key, final Object value);

    Object get(String key);

    <T> T get(String key, Class<T> type);

    NitriteId getId();

    Integer getRevision();

    String getSource();

    Long getLastModifiedSinceEpoch();

    Set<String> getFields();

    boolean hasId();

    void remove(String key);

    Document clone();

    int size();

    Document putAll(Document update);

    boolean containsKey(String key);
}
