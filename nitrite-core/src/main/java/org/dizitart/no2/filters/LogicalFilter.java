package org.dizitart.no2.filters;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public abstract class LogicalFilter extends NitriteFilter {
    public abstract List<Filter> getFilters();
}