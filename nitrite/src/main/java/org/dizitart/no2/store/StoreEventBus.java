package org.dizitart.no2.store;

import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;

/**
 * @author Anindya Chatterjee.
 */
class StoreEventBus extends NitriteEventBus<EventInfo, StoreEventListener> {
    @Override
    public void post(EventInfo storeEvent) {
        for (final StoreEventListener listener : getListeners()) {
            getEventExecutor().submit(() -> listener.onEvent(storeEvent));
        }
    }
}
