package org.dizitart.no2.spatial;

import org.dizitart.no2.filters.Filter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * @author Anindya Chatterjee
 */
public class FluentFilter {
    private String field;

    private FluentFilter() {
    }

    public static FluentFilter when(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    public Filter intersects(Geometry geometry) {
        return new IntersectsFilter(field, geometry);
    }

    public Filter within(Geometry geometry) {
        return new WithinFilter(field, geometry);
    }

    public Filter near(Coordinate point, Double distance) {
        return new NearFilter(field, point, distance);
    }

    public Filter near(Point point, Double distance) {
        return new NearFilter(field, point, distance);
    }
}
