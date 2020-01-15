package org.dizitart.no2.sync.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.KeyValuePair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
public class DocumentSerializer extends StdScalarSerializer<Document> {

    protected DocumentSerializer() {
        super(Document.class);
    }

    @Override
    public void serialize(Document value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            Map<String, Object> map = fromDocument(value);
            gen.writeObject(map);
        }
    }

    private Map<String, Object> fromDocument(Document document) {
        Map<String, Object> map = new HashMap<>();
        for (KeyValuePair<String, Object> pair : document) {
            Object value = pair.getValue();
            if (value instanceof Document) {
                value = fromDocument((Document) value);
            }
            map.put(pair.getKey(), value);
        }
        return map;
    }
}
