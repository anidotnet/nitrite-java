package org.dizitart.no2.collection;

import org.dizitart.no2.collection.events.ChangeAware;

import java.io.Closeable;

/**
 * @author Anindya Chatterjee.
 */
public interface PersistentCollection<T> extends ChangeAware, MetadataAware, Closeable {
}
