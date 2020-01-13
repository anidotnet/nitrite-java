package org.dizitart.no2.sync;

import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.event.ReplicationEvent;

/**
 * @author Anindya Chatterjee.
 */
public class RemoteOperation implements ConnectionAware {
    private ReplicationConfig config;

    public RemoteOperation(ReplicationConfig config) {
        this.config = config;
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return config.getConnectionConfig();
    }

    public void handleReplicationEvent(ReplicationEvent event) {

    }
}
