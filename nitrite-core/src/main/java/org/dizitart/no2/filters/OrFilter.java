package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;

import java.util.Arrays;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
class OrFilter extends LogicalFilter {
    private Filter rhs;
    private Filter lhs;

    OrFilter(Filter rhs, Filter lhs) {
        this.rhs = rhs;
        this.lhs = lhs;
    }

    @Override
    public List<Filter> getFilters() {
        return Arrays.asList(rhs, lhs);
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        return rhs.apply(element) || lhs.apply(element);
    }
}
