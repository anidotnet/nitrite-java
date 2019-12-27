package org.dizitart.no2.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * @author Anindya Chatterjee
 */
class NearFilter extends WithinFilter {
    NearFilter(String field, Coordinate point, Double distance) {
        super(field, createCircle(point, distance));
    }

    NearFilter(String field, Point point, Double distance) {
        super(field, createCircle(point.getCoordinate(), distance));
    }

    private static Geometry createCircle(Coordinate center, double radius) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(center);
        shapeFactory.setSize(radius * 2);
        return shapeFactory.createCircle();
    }
}
