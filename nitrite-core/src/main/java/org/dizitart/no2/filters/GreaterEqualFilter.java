package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.NumberUtils.compare;

/**
 * @author Anindya Chatterjee
 */
class GreaterEqualFilter extends ComparisonFilter {
    GreaterEqualFilter(String field, Comparable<?> value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof ComparableIndexer && getValue() instanceof Comparable) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                idSet = comparableIndexer.findGreaterEqual(getCollectionName(), getField(), (Comparable) getValue());
            } else {
                if (getValue() instanceof Comparable) {
                    throw new FilterException("gte filter is not supported on indexed field "
                        + getField());
                } else {
                    throw new FilterException(getValue() + " is not comparable");
                }
            }
        }
        return idSet;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        Comparable comparable = getComparable();

        if (getField().equalsIgnoreCase(DOC_ID)) {
            NitriteId nitriteId = null;
            if (comparable instanceof String) {
                nitriteId = NitriteId.createId((String) comparable);
            }

            if (nitriteId != null) {
                return element.getKey().compareTo(nitriteId) >= 0;
            }

        } else {
            Document document = element.getValue();
            Object fieldValue = document.get(getField());
            if (fieldValue != null) {
                if (fieldValue instanceof Number && comparable instanceof Number) {
                    return compare((Number) fieldValue, (Number) comparable) >= 0;
                } else if (fieldValue instanceof Comparable) {
                    Comparable arg = (Comparable) fieldValue;
                    return arg.compareTo(comparable) >= 0;
                } else {
                    throw new FilterException(fieldValue + " is not comparable");
                }
            }
        }

        return false;
    }
}
