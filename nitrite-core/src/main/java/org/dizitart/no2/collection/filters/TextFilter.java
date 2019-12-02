package org.dizitart.no2.collection.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class TextFilter extends StringFilter {
    TextFilter(Field field, String value) {
        super(field, value);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof TextIndexer) {
                TextIndexer textIndexer = (TextIndexer) getIndexer();
                idSet = textIndexer.findText(getCollectionName(), getField(), getStringValue());
            } else {
                throw new FilterException(getValue() + " is not of string type");
            }
        }
        return idSet;
    }

    @Override
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        throw new FilterException(getField() + " is not indexed");
    }
}
