package org.dizitart.no2.sync;

import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class Replica<E> extends AbstractSet<E> implements CollectionEventListener {
    private String replicaId;
    private final Set<Element<E>> additions = new HashSet<>();
    private final Set<Element<E>> updates = new HashSet<>();
    private final Set<Element<E>> tombstone = new HashSet<>();

    public Replica(String replicaId) {
        this.replicaId = replicaId;
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {

    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
