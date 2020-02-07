package org.dizitart.no2.sync.event;

/**
 * @author Anindya Chatterjee
 */
public interface ReplicationEventListener {
    void onEvent(ReplicationEvent event);
}
