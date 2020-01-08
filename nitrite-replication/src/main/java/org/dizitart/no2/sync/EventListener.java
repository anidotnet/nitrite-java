package org.dizitart.no2.sync;

/**
 * @author Anindya Chatterjee.
 */
public interface EventListener {
    void onEvent(ReplicationEvent event);
}
