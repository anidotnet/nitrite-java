package org.dizitart.no2.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteSerializable extends Serializable {
    void writeObject(ObjectOutputStream stream) throws IOException;
    void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
