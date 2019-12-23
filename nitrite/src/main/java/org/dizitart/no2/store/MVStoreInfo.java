package org.dizitart.no2.store;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.KeyValuePair;

import java.util.HashMap;
import java.util.Map;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.Constants.DOC_MODIFIED;

/**
 * @author Anindya Chatterjee
 */
class MVStoreInfo implements StoreInfo {
    private final Map<String, String> info;

    MVStoreInfo(Document document) {
        this.info = new HashMap<>();
        populateInfo(document);
    }

    @Override
    public Map<String, String> getInfo() {
        return info;
    }

    private void populateInfo(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_SOURCE);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);

        for (KeyValuePair<String, Object> keyValuePair : document) {
            info.put(keyValuePair.getKey(), keyValuePair.getValue().toString());
        }
    }
}
