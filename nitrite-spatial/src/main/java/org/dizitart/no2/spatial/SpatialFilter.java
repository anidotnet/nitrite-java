package org.dizitart.no2.spatial;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.IndexAwareFilter;
import org.locationtech.jts.geom.Geometry;

/**
 * @author Anindya Chatterjee
 */
public abstract class SpatialFilter extends IndexAwareFilter {
    private Geometry geometry;

    protected SpatialFilter(String field, Geometry geometry) {
        super(field, geometry);
        this.geometry = geometry;
    }

    @Override
    public Geometry getValue() {
        return geometry;
    }

    @Override
    protected boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element) {
        throw new FilterException(getField() + " is not indexed with Spatial index");
    }
}
