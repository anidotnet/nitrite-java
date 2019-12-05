package org.dizitart.no2.mapper;

import org.dizitart.no2.collection.Document;

/**
 * An object that serializes itself to a {@link Document}
 * and vice versa.
 *
 * @author Anindya Chatterjee
 * @since 2.0
 */
public interface Mappable {
    /**
     * Writes the instance data to a {@link Document} and returns it.
     *
     * @param mapper the {@link NitriteMapper} instance used.
     * @return the document generated.
     */
    Document write(NitriteMapper mapper);

    /**
     * Reads the `document` and populate all fields of this instance.
     *
     * @param mapper   the {@link NitriteMapper} instance used.
     * @param document the document.
     */
    void read(NitriteMapper mapper, Document document);
}
