package org.dizitart.no2.collection.filters;

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.util.ValidationUtils;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
@Getter
@ToString
class NotInFilter extends BaseFilter {
    private Set<Comparable> comparableSet;

    NotInFilter(String field, Comparable... values) {
        super(field, values);
        this.comparableSet = new HashSet<>();
        Collections.addAll(this.comparableSet, values);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        validateNotInFilterValue(getField(), comparableSet);

        this.comparableSet = convertValues(this.comparableSet);

        if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField()) && comparableSet != null) {
            ComparableIndexer comparableIndexer = getIndexedQueryTemplate().getComparableIndexer();
            return comparableIndexer.findNotIn(getField(), comparableSet);
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = document.get(getField());

            if (fieldValue instanceof Comparable) {
                Comparable comparable = (Comparable) fieldValue;
                if (!comparableSet.contains(comparable)) {
                    nitriteIdSet.add(entry.getKey());
                }
            }
        }
        return nitriteIdSet;
    }

    private void validateNotInFilterValue(String field, Collection<Comparable> values) {
        ValidationUtils.notNull(field, errorMessage("field cannot be null", VE_NOT_IN_FILTER_NULL_FIELD));
        ValidationUtils.notEmpty(field, errorMessage("field cannot be empty", VE_NOT_IN_FILTER_EMPTY_FIELD));
        ValidationUtils.notNull(values, errorMessage("values cannot be null", VE_NOT_IN_FILTER_NULL_VALUES));
        if (values.size() == 0) {
            throw new ValidationException(errorMessage("values cannot be empty", VE_NOT_IN_FILTER_EMPTY_VALUES));
        }
    }
}
