package org.dizitart.no2.sync.event;

import org.dizitart.no2.common.concurrent.ExecutorServiceManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee
 */
public class MessageRouter {
    private Map<String, Set<ReplicationEventListener>> registry;

    public MessageRouter() {
        this.registry = new ConcurrentHashMap<>();
    }

    public void addListener(ReplicationEventListener replicationEventListener) {
        String name = replicationEventListener.getName();
        Set<ReplicationEventListener> eventListeners;
        if (registry.containsKey(name)) {
            eventListeners = registry.get(name);
        } else {
            eventListeners = new HashSet<>();
        }

        eventListeners.add(replicationEventListener);
        registry.put(name, eventListeners);
    }

    public void dispatch(ReplicationEvent replicationEvent) {
        String collection = replicationEvent.getMessage().getMessageHeader().getCollection();
        Set<ReplicationEventListener> eventListeners = registry.get(collection);
        ExecutorService dispatcher = ExecutorServiceManager.getThreadPool(1, SYNC_THREAD_NAME);

        if (eventListeners != null) {
            for (final ReplicationEventListener listener : eventListeners) {
                dispatcher.submit(() -> listener.onEvent(replicationEvent));
            }
        }
    }

    public void removeListener(ReplicationEventListener replicationEventListener) {
        String name = replicationEventListener.getName();
        if (registry.containsKey(name)) {
            registry.get(name).remove(replicationEventListener);
        }
    }
}
