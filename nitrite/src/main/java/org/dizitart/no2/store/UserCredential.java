package org.dizitart.no2.store;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
class UserCredential implements Serializable {
    private static final long serialVersionUID = -8798544358192824615L;

    private byte[] passwordHash;
    private byte[] passwordSalt;
}
