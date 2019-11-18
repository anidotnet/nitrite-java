package org.dizitart.no2;

import lombok.EqualsAndHashCode;
import org.dizitart.no2.exceptions.InvalidIdException;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.common.Constants.ID_PREFIX;
import static org.dizitart.no2.common.Constants.ID_SUFFIX;
import static org.dizitart.no2.exceptions.ErrorMessage.CAN_NOT_COMPARE_WITH_NULL_ID;
import static org.dizitart.no2.exceptions.ErrorMessage.ID_VALUE_CAN_NOT_BE_NULL;

/**
 * An unique identifier across the Nitrite database. Each document in
 * a nitrite collection is associated with a {@link NitriteId}.
 *
 * During insertion if an unique object is supplied in the '_id' field
 * of the document, then the value of the '_id' field will be used to
 * create a new {@link NitriteId}. If that is not supplied, then nitrite
 * will auto generate one and supply it in the '_id' field of the document.
 *
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#getById(NitriteId)
 * @since 1.0
 */
@EqualsAndHashCode
public final class NitriteId implements Comparable<NitriteId>, Serializable {
    private static final long serialVersionUID = 1477462375L;
    private static final AtomicLong counter = new AtomicLong(System.nanoTime());

    private Long idValue;

    private NitriteId() {
        idValue = counter.getAndIncrement();
    }

    private NitriteId(Long value) {
        idValue = value;
    }

    /**
     * Gets a new auto-generated {@link NitriteId}.
     *
     * @return a new auto-generated {@link NitriteId}.
     */
    public static NitriteId newId() {
        return new NitriteId();
    }

    /**
     * Creates a {@link NitriteId} from a long value.
     *
     * @param value the value
     * @return the {@link NitriteId}
     */
    public static NitriteId createId(Long value) {
        if (value == null) {
            throw new InvalidIdException(ID_VALUE_CAN_NOT_BE_NULL);
        }

        return new NitriteId(value);
    }

    @Override
    public int compareTo(NitriteId other) {
        if (other.idValue == null) {
            throw new InvalidIdException(CAN_NOT_COMPARE_WITH_NULL_ID);
        }

        return Long.compare(idValue, other.idValue);
    }

    @Override
    public String toString() {
        if (idValue != null) {
            return ID_PREFIX + idValue.toString() + ID_SUFFIX;
        }
        return "";
    }

    /**
     * Gets the underlying id object.
     *
     * @return the underlying id object.
     */
    public Long getIdValue() {
        if (idValue != null) return idValue;
        return null;
    }
}
