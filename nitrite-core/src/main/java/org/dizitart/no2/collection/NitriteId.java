package org.dizitart.no2.collection;

import lombok.EqualsAndHashCode;
import org.dizitart.no2.exceptions.InvalidIdException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.common.Constants.ID_PREFIX;
import static org.dizitart.no2.common.Constants.ID_SUFFIX;

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
    private static final long hardwareId = getSystemId();
    private static final AtomicLong counter = new AtomicLong(0);

    private Long idValue;

    private NitriteId() {
        this.idValue = generateLongId();
    }

    private NitriteId(Long value) {
        this.idValue = value;
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
            throw new InvalidIdException("id cannot be null");
        }

        return new NitriteId(value);
    }

    @Override
    public int compareTo(NitriteId other) {
        if (other.idValue == null) {
            throw new InvalidIdException("cannot compare with null id");
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
        return idValue;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeLong(idValue);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        idValue = stream.readLong();
    }

    private synchronized Long generateLongId() {
        long timestamp = System.currentTimeMillis();
        return ((timestamp - 1288834974657L) << 22L) |
            (hardwareId << 12) |
            counter.getAndIncrement();
    }

    private static long getSystemId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            long id;
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                id = ((0x000000FF & (long) mac[mac.length - 1])
                    | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
            }
            return id;
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException("failed to extract hardware id", e);
        }
    }
}
