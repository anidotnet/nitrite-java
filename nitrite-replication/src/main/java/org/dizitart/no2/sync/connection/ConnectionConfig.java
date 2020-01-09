package org.dizitart.no2.sync.connection;

/**
 * @author Anindya Chatterjee
 */
public interface ConnectionConfig {
    String getUrl();
    TimeSpan getConnectTimeout();
    AuthType getAuthType();
    String getAuthToken();
}
