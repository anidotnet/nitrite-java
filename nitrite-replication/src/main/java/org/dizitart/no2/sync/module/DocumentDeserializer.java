package org.dizitart.no2.sync.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.dizitart.no2.collection.Document;

import java.io.IOException;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
public class DocumentDeserializer extends JsonDeserializer<Document> {

    @Override
    public Document deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, Object> map = p.readValueAs(new TypeReference<Map<String, Object>>() {
        });
        return toDocument(map);
    }

    @SuppressWarnings("unchecked")
    private Document toDocument(Map<String, Object> map) {
        Document document = Document.createDocument();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = toDocument((Map<String, Object>) value);
            }
            document.put(entry.getKey(), value);
        }
        return document;
    }
}
