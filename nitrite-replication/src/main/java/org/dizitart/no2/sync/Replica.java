package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public final class Replica implements AutoCloseable {
    private ReplicationTemplate replicationTemplate;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(Config config) {
        this.replicationTemplate = new ReplicationTemplate(config);
    }

    public void connect() {
        try {
            replicationTemplate.connect();
        } catch (ReplicationException re) {
            throw re;
        } catch (Exception e) {
            log.error("Error while connecting the replica {}", getReplicaId(), e);
            throw new ReplicationException("failed to open connection", e, true);
        }
    }

    public void disconnect() {
        try {
            replicationTemplate.disconnect();
        } catch (ReplicationException re) {
            throw re;
        } catch (Exception e) {
            log.error("Error while disconnecting the replica {}", getReplicaId(), e);
            throw new ReplicationException("failed to disconnect the replica", e, true);
        }
    }

    private String getReplicaId() {
        return replicationTemplate.getReplicaId();
    }

    @Override
    public void close() {
        replicationTemplate.stopReplication("Normal shutdown");
    }
}
