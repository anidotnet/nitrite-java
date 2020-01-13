package org.dizitart.no2.sync.connection;

/**
 * @author Anindya Chatterjee.
 */
public interface ConnectionAware {
    ConnectionConfig getConnectionConfig();

    default Connection getConnection() {
        return ConnectionPool.getInstance().getConnection(getConnectionConfig());
    }
}
