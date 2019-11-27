package org.dizitart.no2.store;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
class UserCredential implements Serializable {
    private byte[] passwordHash;
    private byte[] passwordSalt;
}
