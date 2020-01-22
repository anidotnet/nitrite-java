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

package org.dizitart.no2.common.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.common.Constants.DAEMON_THREAD_NAME;

/**
 * A factory for managing for all {@link ExecutorService}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
public class ExecutorServiceManager {
    private static Map<String, ExecutorService> registry;
    private static final Object lock;

    static {
        lock = new Object();
        registry = new ConcurrentHashMap<>();
    }

    /**
     * Creates an {@link ExecutorService} with pull size {@link Runtime#availableProcessors()}
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService commonPool() {
        return getThreadPool(Runtime.getRuntime().availableProcessors(), DAEMON_THREAD_NAME);
    }

    public static ExecutorService getThreadPool(int size, String threadName) {
        String key = threadName + "_" + size;
        if (registry.containsKey(key)) {
            return registry.get(key);
        } else {
            ExecutorService executorService = Executors.newFixedThreadPool(size, threadFactory(threadName));
            registry.put(key, executorService);
            return executorService;
        }
    }

    /**
     * Shuts down and awaits termination of all {@link ExecutorService}s.
     *
     * @param timeout the timeout in seconds
     */
    public static void shutdownExecutors(int timeout) {
        for (ExecutorService value : registry.values()) {
            shutdownAndAwaitTermination(value, timeout);
        }
        registry.clear();
    }

    public static ErrorAwareThreadFactory threadFactory(String name) {
        return new ErrorAwareThreadFactory() {
            @Override
            public Thread createThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName(name);
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    private static void shutdownAndAwaitTermination(final ExecutorService pool, int timeout) {
        synchronized (lock) {
            // Disable new tasks from being submitted
            pool.shutdown();
        }
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                synchronized (lock) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                }
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            synchronized (lock) {
                pool.shutdownNow();
            }
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
