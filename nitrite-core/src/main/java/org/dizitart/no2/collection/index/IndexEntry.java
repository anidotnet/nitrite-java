package org.dizitart.no2.collection.index;

import lombok.*;
import org.dizitart.no2.collection.Field;

import java.io.Serializable;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * Represents a nitrite database index.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * @see org.dizitart.no2.collection.NitriteCollection#createIndex(org.dizitart.no2.collection.Field, org.dizitart.no2.collection.IndexOptions)
 */
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexEntry implements Comparable<IndexEntry>, Serializable {

    /**
     * Specifies the type of the index.
     *
     * @return the type of the index.
     * @see IndexType
     * */
    @Getter
    private String indexType;

    /**
     * Gets the target field for the index.
     *
     * @return the target field.
     * */
    @Getter
    private Field field;

    /**
     * Gets the collection name.
     *
     * @return the collection name.
     * */
    @Getter
    private String collectionName;

    /**
     * Instantiates a new Index.
     *
     * @param indexType      the index type
     * @param field          the value
     * @param collectionName the collection name
     */
    public IndexEntry(String indexType, Field field, String collectionName) {
        notNull(indexType, errorMessage("indexType cannot be null", VE_INDEX_NULL_INDEX_TYPE));
        notNull(field, errorMessage("field cannot be null", VE_INDEX_NULL_FIELD));
        notNull(collectionName, errorMessage("collectionName cannot be null", VE_INDEX_NULL_COLLECTION));
        notEmpty(collectionName, errorMessage("collectionName cannot be empty", VE_INDEX_EMPTY_COLLECTION));

        this.indexType = indexType;
        this.field = field;
        this.collectionName = collectionName;
    }

    @Override
    public int compareTo(IndexEntry other) {
        String string = collectionName + field + indexType;
        String otherString = other.collectionName + other.field + other.indexType;
        return string.compareTo(otherString);
    }
}
