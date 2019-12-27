package org.dizitart.no2.index;

import org.dizitart.no2.common.NitriteSerializable;

/**
 * @author Anindya Chatterjee
 */
public interface BoundingBox extends NitriteSerializable {
    float getMinX();
    float getMaxX();
    float getMinY();
    float getMaxY();
}
