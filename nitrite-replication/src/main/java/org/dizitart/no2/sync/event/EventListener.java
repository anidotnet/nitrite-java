package org.dizitart.no2.sync.event;

/**
 * @author Anindya Chatterjee.
 */
public interface EventListener {
    void onEvent(ReplicationEvent event);
}
