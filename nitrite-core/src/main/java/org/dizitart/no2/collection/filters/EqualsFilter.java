package org.dizitart.no2.collection.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.ComparableIndexer;
import org.dizitart.no2.common.KeyValuePair;

import static org.dizitart.no2.common.Constants.DOC_ID;

/**
 * @author Anindya Chatterjee.
 */
class EqualsFilter extends FieldBasedFilter {
    EqualsFilter(Field field, Object value) {
        super(field, value);
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        Object value = getValue();
        NitriteId nitriteId = null;

        if (getField().getName().equalsIgnoreCase(DOC_ID)) {
            if (value instanceof Long) {
                nitriteId = NitriteId.createId((Long) value);
            }
        } else if (getIndexOperations().hasIndexEntry(getField())
            && !getIndexOperations().isIndexing(getField())
            && value != null) {

            if (getIndexer() instanceof ComparableIndexer) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                nitriteId = comparableIndexer.findByEqual(value);
            }
        }

        if (nitriteId != null) {
            if (element.getKey().equals(nitriteId)) {
                return true;
            }
        }
    }
}
