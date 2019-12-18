package org.dizitart.no2.store;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
class UserCredential implements Serializable {
    private static final long serialVersionUID = 1576690755L;

    private byte[] passwordHash;
    private byte[] passwordSalt;
}
