/*
 *  Copyright 2017-2019 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.event;

import org.dizitart.no2.common.concurrent.ExecutorServiceManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * An abstract implementation of {@link EventBus}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public final class NitriteEventBus implements EventBus {
    private static NitriteEventBus eventBus = new NitriteEventBus();

    private Set<EventListener> listeners;
    private ExecutorService eventExecutor;


    /**
     * Get nitrite event bus.
     *
     * @return the nitrite event bus.
     */
    public static NitriteEventBus get() {
        return eventBus;
    }

    private NitriteEventBus() {
        this.listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.register(event -> {
            if (event instanceof DatabaseEvent.Closed) {
                this.close();
            }
        });
    }

    @Override
    public void register(EventListener eventListener) {
        if (eventListener != null) {
            listeners.add(eventListener);
        }
    }

    @Override
    public void deregister(EventListener eventListener) {
        if (eventListener != null) {
            listeners.remove(eventListener);
        }
    }

    @Override
    public void post(EventInfo eventInfo) {
        for (final EventListener listener : getListeners()) {
            String threadName = Thread.currentThread().getName();
            eventInfo.setOriginator(threadName);
            getEventExecutor().submit(() -> listener.onEvent(eventInfo));
        }
    }

    @Override
    public void close() {
        listeners.clear();
    }

    private ExecutorService getEventExecutor() {
        if (eventExecutor == null) {
            eventExecutor = ExecutorServiceManager.commonPool();
        }
        return eventExecutor;
    }

    private Set<EventListener> getListeners() {
        return listeners;
    }
}
