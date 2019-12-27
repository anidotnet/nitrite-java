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

package org.dizitart.no2.mapper;

import org.dizitart.no2.collection.NitriteId;

/**
 * Class that registers capability of serializing {@code NitriteId} with the Jackson core.
 *
 * @since 1.0.0
 * @author Anindya Chatterjee
 */
public class NitriteIdModule extends SimpleNitriteModule {

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(NitriteId.class, new NitriteIdSerializer());
        addDeserializer(NitriteId.class, new NitriteIdDeserializer());
        super.setupModule(context);
    }

    @Override
    public Class<?> getDataType() {
        return NitriteId.class;
    }
}
