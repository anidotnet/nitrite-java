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

package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.repository.Cursor;

/**
 * Represents lookup parameters in join operation of two collections.
 *
 * @author Anindya Chatterjee.
 * @see DocumentCursor#join(DocumentCursor, Lookup)
 * @see Cursor#join(Cursor, Lookup, Class)
 * @since 2.1.0
 */
public class Lookup {

    /**
     * Specifies the field from the records input to the join.
     *
     * @param localField field of the input record.
     * @returns field of the input record.
     */
    @Getter
    @Setter
    private String localField;

    /**
     * Specifies the field from the foreign records.
     *
     * @param foreignField field of the foreign record.
     * @returns field of the foreign record.
     */
    @Getter
    @Setter
    private String foreignField;

    /**
     * Specifies the new field of the joined records.
     *
     * @param targetField field of the joined record.
     * @returns field of the joined record.
     */
    @Getter
    @Setter
    private String targetField;
}
