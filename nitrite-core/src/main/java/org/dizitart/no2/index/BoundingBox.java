package org.dizitart.no2.index;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
public interface BoundingBox extends Serializable {
    float getMinX();

    float getMaxX();

    float getMinY();

    float getMaxY();
}
