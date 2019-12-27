package org.dizitart.no2.spatial;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.FilterException;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class WithinFilter extends SpatialFilter {
    protected WithinFilter(String field, Geometry geometry) {
        super(field, geometry);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof SpatialIndexer && getValue() != null) {
                SpatialIndexer spatialIndexer = (SpatialIndexer) getIndexer();
                ReadableStream<NitriteId> idSet = spatialIndexer.findWithin(getCollectionName(), getField(), getValue());
                return idSet.toSet();
            } else {
                throw new FilterException(getValue() + " is not a Geometry");
            }
        }
        return new LinkedHashSet<>();
    }
}
