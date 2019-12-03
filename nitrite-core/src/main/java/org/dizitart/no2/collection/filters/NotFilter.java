package org.dizitart.no2.collection.filters;

import lombok.Getter;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.KeyValuePair;

/**
 * @author Anindya Chatterjee
 */
class NotFilter extends NitriteFilter {
    @Getter
    private Filter filter;

    NotFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        return !filter.apply(element);
    }
}