package org.dizitart.no2.sync.crdt;

import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.dizitart.no2.sync.crdt.Timestamp.compare;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class LastWriteWinMap<Key, Value> implements Serializable {
    private transient ReentrantLock lock;
    private int clock = 0;
    private String uid;
    private Map<Key, Entry<Key, Value>> additions;
    private Map<Key, Timestamp> tombstones;

    public LastWriteWinMap() {
        this(UUID.randomUUID().toString());
    }

    public LastWriteWinMap(String uid) {
        this.uid = uid;
        this.lock = new ReentrantLock();
        this.additions = new LinkedHashMap<>();
        this.tombstones = new LinkedHashMap<>();
    }

    public LastWriteWinMap(Map<Key, Entry<Key, Value>> additions,
                           Map<Key, Timestamp> tombstones) {
        this.additions = additions;
        this.tombstones = tombstones;
    }

    public Value get(Key key) {
        if (!additions.containsKey(key)) return null;
        else {
            if (tombstones.containsKey(key)
            && compare(tombstones.get(key), additions.get(key).getTimestamp()) > 0) {
                return null;
            } else {
                return additions.get(key).getValue();
            }
        }
    }

    public void put(Key key, Value value) {
        clock++;
        Timestamp now = new Timestamp(System.currentTimeMillis(), clock);
        Entry<Key, Value> entry = new Entry<>(key, value, now);
        this.additions.put(key, entry);
    }

    public void remove(Key key) {
        clock++;
        Timestamp now = new Timestamp(System.currentTimeMillis(), clock);
        this.tombstones.put(key, now);
    }

    public boolean merge(LastWriteWinMap<Key, Value> remote) {
        boolean update = false;
        try {
            lock.lock();
            for (Key key : remote.additions.keySet()) {
                if (!additions.containsKey(key)) {
                    additions.put(key, remote.additions.get(key));
                    update = true;
                } else if (compare(additions.get(key).getTimestamp(), remote.additions.get(key).getTimestamp()) < 0) {
                    additions.put(key, remote.additions.get(key));
                    update = true;
                }
            }

            for (Key key : remote.tombstones.keySet()) {
                if (!tombstones.containsKey(key)) {
                    tombstones.put(key, remote.tombstones.get(key));
                    update = true;
                } else if (compare(tombstones.get(key), remote.tombstones.get(key)) < 0) {
                    tombstones.put(key, remote.tombstones.get(key));
                }
            }
        } finally {
            lock.unlock();
        }
        return update;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(clock);
        stream.writeUTF(uid);
        stream.writeObject(additions);
        stream.writeObject(tombstones);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        clock = stream.readInt();
        uid = stream.readUTF();
        additions = (Map<Key, Entry<Key, Value>>) stream.readObject();
        tombstones = (Map<Key, Timestamp>) stream.readObject();
    }
}
