package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.TextIndexer;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class EqualsFilter extends IndexAwareFilter {
    EqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getValue() == null || getValue() instanceof Comparable) {
                if (getIndexer() instanceof ComparableIndexer) {
                    ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                    idSet = comparableIndexer.findEqual(getCollectionName(), getField(), (Comparable) getValue());
                } else if (getIndexer() instanceof TextIndexer && getValue() instanceof String) {
                    // eq filter is not compatible with TextIndexer
                    setIsFieldIndexed(false);
                } else {
                    throw new FilterException("eq filter is not supported on indexed field "
                        + getField());
                }
            } else {
                throw new FilterException(getValue() + " is not comparable");
            }
        }
        return idSet;
    }

    @Override
    public boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        Object value = getValue();

        if (getField().equalsIgnoreCase(DOC_ID)) {
            if (value instanceof String) {
                return Objects.equals(element.getKey().getIdValue(), getValue());
            }
        } else {
            Document document = element.getValue();
            Object fieldValue = document.get(getField());
            return deepEquals(fieldValue, value);
        }

        return false;
    }
}
