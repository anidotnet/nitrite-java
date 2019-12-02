package org.dizitart.no2.collection.filters;

import lombok.Getter;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.ValidationException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class NotInFilter extends IndexAwareFilter {
    @Getter
    @SuppressWarnings("rawtypes")
    private Set<Comparable> comparableSet;

    NotInFilter(Field field, Comparable<?>... values) {
        super(field, values);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof ComparableIndexer && comparableSet != null) {
                ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                idSet = comparableIndexer.findNotIn(getCollectionName(), getField(), comparableSet);
            } else {
                if (comparableSet != null && !comparableSet.isEmpty()) {
                    throw new FilterException("notIn filter is not supported on indexed field "
                        + getField().getName());
                } else {
                    throw new FilterException("invalid notIn filter");
                }
            }
        }
        return idSet;
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        validateNotInFilterValue(getField(), comparableSet);
        this.comparableSet = convertValues(this.comparableSet);
        return super.apply(element);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        Document document = element.getValue();
        Object fieldValue = document.get(getField().getName());

        if (fieldValue instanceof Comparable) {
            Comparable comparable = (Comparable) fieldValue;
            return !comparableSet.contains(comparable);
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private void validateNotInFilterValue(Field field, Collection<Comparable> values) {
        notNull(field, "field cannot be null");
        notNull(values, "values cannot be null");
        if (values.size() == 0) {
            throw new ValidationException("values cannot be empty");
        }
    }
}
