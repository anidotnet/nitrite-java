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

package org.dizitart.no2.exceptions;

/**
 * Exception thrown when a database security error occurs.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class SecurityException extends NitriteException {
    /**
     * Instantiates a new {@link SecurityException}.
     *
     * @param errorMessage the error message
     */
    public SecurityException(String errorMessage) {
        super(errorMessage);
    }
}
