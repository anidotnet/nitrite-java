package org.dizitart.no2.common;

/**
 * An enum to specify where to place `null` values during sort.
 *
 * @since 3.1.0
 * @author Anindya Chatterjee
 */
public enum NullOrder {
    /**
     * Places `null` values at first.
     */
    First,

    /**
     * Places `null` values at last.
     */
    Last,

    /**
     * Places `null` values at default location.
     */
    Default
}
