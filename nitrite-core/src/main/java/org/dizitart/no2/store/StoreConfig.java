package org.dizitart.no2.store;

import org.dizitart.no2.common.util.StringUtils;

/**
 * Represents a {@link NitriteStore} configuration.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
public interface StoreConfig {
    /**
     * Gets file path for the store.
     *
     * @return the file path
     */
    String getFilePath();

    /**
     * Indicates if the {@link NitriteStore} is a readonly store.
     *
     * @return `true`, if readonly store; otherwise `false`.
     */
    boolean isReadOnly();

    /**
     * Indicates if the {@link NitriteStore} is an in-memory store.
     *
     * @return `true`, if in-memory store; otherwise `false`.
     */
    default boolean isInMemory() {
        return StringUtils.isNullOrEmpty(getFilePath());
    }
}
