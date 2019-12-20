package org.dizitart.no2.store;

import org.dizitart.no2.common.NitriteSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
public class StoreInfo implements NitriteSerializable {
    private Map<String, String> info = new HashMap<>();

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(info);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        info = (Map<String, String>) stream.readObject();
    }
}
