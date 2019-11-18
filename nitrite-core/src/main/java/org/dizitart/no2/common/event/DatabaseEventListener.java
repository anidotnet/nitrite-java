package org.dizitart.no2.common.event;

/**
 * @author Anindya Chatterjee
 */
public interface DatabaseEventListener {
    void onEvent(DatabaseEvent event);
}
