package org.dizitart.no2.collection.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.NumberUtils.compare;

/**
 * @author Anindya Chatterjee
 */
class LesserThanFilter extends ComparisonFilter {
    LesserThanFilter(Field field, Comparable<?> value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof ComparableIndexer && getValue() instanceof Comparable) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                idSet = comparableIndexer.findLesserThan(getCollectionName(), getField(), (Comparable) getValue());
            } else {
                if (getValue() instanceof Comparable) {
                    throw new FilterException("lt filter is not supported on indexed field "
                        + getField().getName());
                } else {
                    throw new FilterException(getValue() + " is not comparable");
                }
            }
        }
        return idSet;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        Comparable comparable = getComparable();

        if (getField().getName().equalsIgnoreCase(DOC_ID)) {
            NitriteId nitriteId = null;
            if (comparable instanceof Long) {
                nitriteId = NitriteId.createId((Long) comparable);
            }

            if (nitriteId != null) {
                return element.getKey().compareTo(nitriteId) < 0;
            }

        } else {
            Document document = element.getValue();
            Object fieldValue = document.get(getField().getName());
            if (fieldValue != null) {
                if (fieldValue instanceof Number && comparable instanceof Number) {
                    return compare((Number) fieldValue, (Number) comparable) < 0;
                } else if (fieldValue instanceof Comparable) {
                    Comparable arg = (Comparable) fieldValue;
                    return arg.compareTo(comparable) < 0;
                } else {
                    throw new FilterException(fieldValue + " is not comparable");
                }
            }
        }

        return false;
    }
}
