package org.dizitart.no2.sync.event;

/**
 * @author Anindya Chatterjee.
 */
public interface ReplicationEventListener {
    String getName();
    void onEvent(ReplicationEvent event);
}
