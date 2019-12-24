/*
 * Copyright 2017-2019 Nitrite author or authors.
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

package org.dizitart.no2.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.io.IOException;
import java.util.*;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class JacksonMapper implements NitriteMapper {
    private MappableMapper mappableMapper;
    private Set<Module> modules;
    private ObjectMapper objectMapper;

    public JacksonMapper() {
        this(new MappableMapper());
    }

    public JacksonMapper(MappableMapper mappableMapper) {
        this(new HashSet<>(), mappableMapper);
    }

    public JacksonMapper(Set<Module> modules) {
        this(modules, new MappableMapper());
    }

    public JacksonMapper(Set<Module> modules, MappableMapper mappableMapper) {
        this.mappableMapper = mappableMapper;
        this.modules = modules;
        this.objectMapper = createObjectMapper();
    }

    protected ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(
            objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new NitriteIdModule());

        for (Module module : modules) {
            objectMapper.registerModule(module);
        }

        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> Target convert(Source source, Class<Target> type) {
        if (source == null) {
            return null;
        }

        if (isValue(source)) {
            return (Target) convertValue(source);
        } else {
            if (Document.class.isAssignableFrom(type)) {
                return (Target) convertToDocument(source);
            } else if (source instanceof Document) {
                return convertToObject((Document) source, type);
            }
        }

        throw new ObjectMappingException("failed to convert using jackson");
    }

    @Override
    public boolean isValueType(Class<?> type) {
        if (mappableMapper.isValueType(type)) return true;
        Object item = ObjectUtils.newInstance(type, false);
        return isValue(item);
    }

    @Override
    public boolean isValue(Object object) {
        JsonNode node = objectMapper.convertValue(object, JsonNode.class);
        return node != null && node.isValueNode();
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }

    private <Target> Target convertToObject(Document source, Class<Target> type) {
        if (Mappable.class.isAssignableFrom(type)) {
            Target item = newInstance(type, false);
            if (item == null) return null;

            ((Mappable) item).read(this, source);
            return item;
        }

        try {
            return objectMapper.convertValue(source, type);
        } catch (IllegalArgumentException iae) {
            log.error("Error while converting document to object ", iae);
            if (iae.getCause() instanceof JsonMappingException) {
                JsonMappingException jme = (JsonMappingException) iae.getCause();
                if (jme.getMessage().contains("Cannot construct instance")) {
                    throw new ObjectMappingException(jme.getMessage());
                }
            }
            throw iae;
        }
    }

    private <Source> Object convertToDocument(Source source) {
        JsonNode node = objectMapper.convertValue(source, JsonNode.class);
        return loadDocument(node);
    }

    private Object convertValue(Object object) {
        JsonNode node = objectMapper.convertValue(object, JsonNode.class);
        if (node == null) {
            return null;
        }

        switch (node.getNodeType()) {
            case NUMBER:
                return node.numberValue();
            case STRING:
                return node.textValue();
            case BOOLEAN:
                return node.booleanValue();
            case ARRAY:
            case BINARY:
            case MISSING:
            case NULL:
            case OBJECT:
            case POJO:
            default:
                return null;
        }
    }

    private Document loadDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = loadObject(value);
            objectMap.put(name, object);
        }

        return Document.createDocument(objectMap);
    }

    private Object loadObject(JsonNode node) {
        if (node == null)
            return null;
        try {
            switch (node.getNodeType()) {
                case ARRAY:
                    return loadArray(node);
                case BINARY:
                    return node.binaryValue();
                case BOOLEAN:
                    return node.booleanValue();
                case MISSING:
                case NULL:
                    return null;
                case NUMBER:
                    return node.numberValue();
                case OBJECT:
                case POJO:
                    return loadDocument(node);
                case STRING:
                    return node.textValue();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List loadArray(JsonNode array) {
        if (array.isArray()) {
            List list = new ArrayList();
            Iterator iterator = array.elements();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof JsonNode) {
                    list.add(loadObject((JsonNode) element));
                } else {
                    list.add(element);
                }
            }
            return list;
        }
        return null;
    }
}
