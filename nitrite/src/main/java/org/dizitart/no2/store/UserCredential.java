package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.common.NitriteSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class UserCredential implements NitriteSerializable {
    private static final long serialVersionUID = 1576690755L;

    private byte[] passwordHash;
    private byte[] passwordSalt;

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(passwordHash);
        stream.writeObject(passwordSalt);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        passwordHash = (byte[]) stream.readObject();
        passwordSalt = (byte[]) stream.readObject();
    }
}
