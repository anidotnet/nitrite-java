package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.repository.Cursor;

/**
 * Represents lookup parameters in join operation of two collections.
 *
 * @author Anindya Chatterjee.
 * @since 2.1.0
 * @see DocumentCursor#join(DocumentCursor, Lookup)
 * @see Cursor#join(Cursor, Lookup, Class)
 */
public class Lookup {

    /**
     * Specifies the field from the records input to the join.
     *
     * @param localField field of the input record.
     * @returns field of the input record.
     * */
    @Getter
    @Setter
    private String localField;

    /**
     * Specifies the field from the foreign records.
     *
     * @param foreignField field of the foreign record.
     * @returns field of the foreign record.
     * */
    @Getter @Setter
    private String foreignField;

    /**
     * Specifies the new field of the joined records.
     *
     * @param targetField field of the joined record.
     * @returns field of the joined record.
     * */
    @Getter @Setter
    private String targetField;
}
