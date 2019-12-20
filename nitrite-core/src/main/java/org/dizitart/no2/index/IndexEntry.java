package org.dizitart.no2.index;

import lombok.*;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.NitriteSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a nitrite database index.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * @see NitriteCollection#createIndex(String, IndexOptions)
 */
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexEntry implements Comparable<IndexEntry>, NitriteSerializable {
    private static final long serialVersionUID = 1576690829L;

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
    private String field;

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
    public IndexEntry(String indexType, String field, String collectionName) {
        notNull(indexType, "indexType cannot be null");
        notNull(field, "field cannot be null");
        notNull(collectionName, "collectionName cannot be null");
        notEmpty(collectionName, "collectionName cannot be empty");

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

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(indexType);
        stream.writeUTF(field);
        stream.writeUTF(collectionName);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException {
        indexType = stream.readUTF();
        field = stream.readUTF();
        collectionName = stream.readUTF();
    }
}
