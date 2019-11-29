package org.dizitart.no2.collection.filters;

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.ComparableIndexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQUAL_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQ_NOT_SUPPORTED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class EqualsFilter extends IndexAwareFilter {
    EqualsFilter(Field field, Object value) {
        super(field, value);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof ComparableIndexer && getValue() instanceof Comparable) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                idSet = comparableIndexer.findEqual(getCollectionName(), getField(), (Comparable) getValue());
            } else {
                if (getValue() instanceof Comparable) {
                    throw new FilterException(errorMessage("eq filter is not supported on indexed field "
                        + getField().getName(), FE_EQ_NOT_SUPPORTED));
                } else {
                    throw new FilterException(errorMessage(getValue() + " is not comparable",
                        FE_EQUAL_NOT_COMPARABLE));
                }
            }
        }
        return idSet;
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        Object value = getValue();

        if (getField().getName().equalsIgnoreCase(DOC_ID)) {
            if (value instanceof Long) {
                return Objects.equals(element.getKey().getIdValue(), getValue());
            }
        } else {
            if (getIsFieldIndexed()) {
                return getIndexedIdSet().contains(element.getKey());
            } else {
                Document document = element.getValue();
                Object fieldValue = document.get(getField().getName());
                return deepEquals(fieldValue, value);
            }
        }

        return false;
    }
}
