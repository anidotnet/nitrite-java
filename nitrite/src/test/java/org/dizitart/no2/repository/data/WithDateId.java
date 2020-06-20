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

package org.dizitart.no2.repository.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@EqualsAndHashCode
public class WithDateId implements Mappable {
    private Date id;
    private String name;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("name", name)
            .put("id", id);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        name = document.get("name", String.class);
        id = document.get("id", Date.class);
    }
}
