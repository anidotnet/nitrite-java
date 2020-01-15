package org.dizitart.no2.sync.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dizitart.no2.collection.Document;

/**
 * @author Anindya Chatterjee
 */
public class DocumentModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        addSerializer(Document.class, new DocumentSerializer());
        addDeserializer(Document.class, new DocumentDeserializer());
        super.setupModule(context);
    }

}
