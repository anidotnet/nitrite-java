package org.dizitart.no2.store.events;

/**
 * @author Anindya Chatterjee
 */
public interface StoreEventListener {
    void onEvent(EventInfo eventInfo);
}
