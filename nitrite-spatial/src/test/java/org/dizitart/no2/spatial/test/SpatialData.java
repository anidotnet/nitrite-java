package org.dizitart.no2.spatial.test;

import lombok.Data;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.locationtech.jts.geom.Geometry;

import static org.dizitart.no2.spatial.SpatialIndexer.SpatialIndex;

/**
 * @author Anindya Chatterjee
 */
@Data
@Index(value = "geometry", type = SpatialIndex)
public class SpatialData {
    @Id
    private Long id;
    private Geometry geometry;
}
