package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.TextIndexer;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class TextFilter extends StringFilter {
    TextFilter(String field, String value) {
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
                throw new FilterException(getField() + " is not full-text indexed");
            }
        }
        return idSet;
    }

    @Override
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        throw new FilterException(getField() + " is not text indexed");
    }
}
