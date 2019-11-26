package org.dizitart.no2.collection.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.KeyValuePair;

/**
 * @author Anindya Chatterjee
 */
class AndFilter extends NitriteFilter {
    private Filter rhs;
    private Filter lhs;

    AndFilter(Filter rhs, Filter lhs) {
        this.rhs = rhs;
        this.lhs = lhs;
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        return rhs.apply(element) && lhs.apply(element);
    }
}
