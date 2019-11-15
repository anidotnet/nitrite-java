package org.dizitart.no2.common.event;

/**
 * @author Anindya Chatterjee
 */
public interface EventListener {
    void onEvent(EventInfo event);

    default void processEvent(EventInfo e) {

    }
}
