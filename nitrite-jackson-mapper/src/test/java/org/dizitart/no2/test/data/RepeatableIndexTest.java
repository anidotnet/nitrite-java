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

package org.dizitart.no2.test.data;

import lombok.Data;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.annotations.Index;

/**
 * @author Anindya Chatterjee
 */
@Data
@Index(value = "firstName")
@Index(value = "age", type = IndexType.NonUnique)
@Index(value = "lastName", type = IndexType.Fulltext)
public class RepeatableIndexTest {
    private String firstName;
    private Integer age;
    private String lastName;
}
