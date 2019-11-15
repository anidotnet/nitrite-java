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
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.event.DatabaseEvent;
import org.dizitart.no2.common.event.NitriteEventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.common.Constants.*;

/**
 * A factory for managing for all {@link ExecutorService}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
public class ExecutorServiceManager {
    private static ExecutorService commonPool;
    private static ScheduledExecutorService scheduledExecutor;
    private static ExecutorService syncExecutor;
    private static final Object lock = new Object();

    static {
        NitriteEventBus eventBus = NitriteEventBus.get();
        eventBus.register(event -> {
            if (event instanceof DatabaseEvent.Closed) {
                NitriteConfig nitriteConfig = ((DatabaseEvent.Closed) event).getNitriteConfig();
                int timeout = nitriteConfig.getPoolShutdownTimeout();
                shutdownExecutors(timeout);
            }
        });
    }

    /**
     * Creates an {@link ExecutorService} with pull size {@link Integer#MAX_VALUE}
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService commonPool() {
        if (commonPool == null || commonPool.isTerminated()) {
            commonPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                    threadFactory(DAEMON_THREAD_NAME));
        }

        return commonPool;
    }

    public static ExecutorService syncExecutor() {
        if (syncExecutor == null || syncExecutor.isTerminated()) {
            syncExecutor = Executors.newFixedThreadPool(1, threadFactory(SYNC_THREAD_NAME));
        }
        return syncExecutor;
    }

    /**
     * Creates a {@link ScheduledExecutorService} with pool size 1
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ScheduledExecutorService}.
     */
    public static ScheduledExecutorService scheduledExecutor() {
        if (scheduledExecutor == null || scheduledExecutor.isTerminated()) {
            scheduledExecutor =
                    Executors.newScheduledThreadPool(1, threadFactory(SCHEDULED_THREAD_NAME));
        }
        return scheduledExecutor;
    }

    /**
     * Shuts down and awaits termination of all {@link ExecutorService}s.
     *
     * @param timeout the timeout in seconds
     */
    private static void shutdownExecutors(int timeout) {
        if (commonPool != null) {
            shutdownAndAwaitTermination(commonPool, timeout);
            commonPool = null;
        }

        if (scheduledExecutor != null) {
            shutdownAndAwaitTermination(scheduledExecutor, timeout);
            scheduledExecutor = null;
        }

        if (syncExecutor != null) {
            shutdownAndAwaitTermination(syncExecutor, timeout);
            syncExecutor = null;
        }
    }

    private static ErrorAwareThreadFactory threadFactory(String name) {
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
