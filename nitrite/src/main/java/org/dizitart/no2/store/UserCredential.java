package org.dizitart.no2.store;

import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class UserCredential implements Serializable {
    private static final long serialVersionUID = 1576690755L;

    private byte[] passwordHash;
    private byte[] passwordSalt;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(passwordHash);
        stream.writeObject(passwordSalt);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        passwordHash = (byte[]) stream.readObject();
        passwordSalt = (byte[]) stream.readObject();
    }
}
