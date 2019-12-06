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

package org.dizitart.no2.repository.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "address", type = IndexType.Fulltext),
        @Index(value = "employeeNote.text", type = IndexType.Fulltext)
})
public class Employee implements Serializable, Mappable {
    @Id
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private transient Company company;

    @Getter
    @Setter
    private byte[] blob;

    @Getter
    @Setter
    private Note employeeNote;

    public Employee() {}

    public Employee(Employee copy) {
        empId = copy.empId;
        joinDate = copy.joinDate;
        address = copy.address;
        company = copy.company;
        blob = copy.blob;
        employeeNote = copy.employeeNote;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument()
            .put("empId", empId)
            .put("joinDate", joinDate != null ? joinDate.getTime() : null)
            .put("address", address)
            .put("blob", blob)
            .put("employeeNote", employeeNote != null ? employeeNote.write(mapper) : null);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        empId = document.get("empId", Long.class);
        if (document.get("joinDate") != null) {
            joinDate = new Date(document.get("joinDate", Long.class));
        }
        address = document.get("address", String.class);
        blob = document.get("blob", byte[].class);

        if (document.get("employeeNote") != null) {
            employeeNote = new Note();
            employeeNote.read(mapper, document.get("employeeNote", Document.class));
        }
    }
}
