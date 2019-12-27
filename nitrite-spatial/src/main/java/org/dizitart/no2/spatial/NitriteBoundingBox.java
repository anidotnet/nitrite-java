package org.dizitart.no2.spatial;

import lombok.Data;
import org.dizitart.no2.index.BoundingBox;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Anindya Chatterjee
 */
@Data
class NitriteBoundingBox implements BoundingBox {
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;

    public NitriteBoundingBox(Geometry geometry) {
        Envelope env = geometry.getEnvelopeInternal();
        this.minX = (float) env.getMinX();
        this.maxX = (float) env.getMaxX();
        this.minY = (float) env.getMinY();
        this.maxY = (float) env.getMaxY();
    }

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeFloat(minX);
        stream.writeFloat(maxX);
        stream.writeFloat(minY);
        stream.writeFloat(maxY);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException {
        this.minX = stream.readFloat();
        this.maxX = stream.readFloat();
        this.minY = stream.readFloat();
        this.maxY = stream.readFloat();
    }
}
