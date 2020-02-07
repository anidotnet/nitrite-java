package org.dizitart.no2.sync.event;

import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.event.NitriteEventBus;

import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee
 */
public class ReplicationEventBus extends NitriteEventBus<ReplicationEvent, ReplicationEventListener> {

    @Override
    public void post(ReplicationEvent replicationEvent) {
        for (final ReplicationEventListener listener : getListeners()) {
            getEventExecutor().submit(() -> listener.onEvent(replicationEvent));
        }
    }

    @Override
    protected ExecutorService getEventExecutor() {
        int core = Runtime.getRuntime().availableProcessors();
        return ThreadPoolManager.getThreadPool(core, SYNC_THREAD_NAME);
    }
}
