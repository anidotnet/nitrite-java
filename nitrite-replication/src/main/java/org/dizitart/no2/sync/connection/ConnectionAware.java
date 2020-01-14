package org.dizitart.no2.sync.connection;

import org.dizitart.no2.sync.ReplicationConfig;

/**
 * @author Anindya Chatterjee.
 */
public interface ConnectionAware {
    ReplicationConfig getConfig();

    default Connection getConnection() {
        return ConnectionPool.getInstance().getConnection(getConfig());
    }
}
