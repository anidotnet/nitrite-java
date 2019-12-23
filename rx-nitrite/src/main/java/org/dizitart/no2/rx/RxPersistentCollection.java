package org.dizitart.no2.rx;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
public interface RxPersistentCollection<T> {
    Completable createIndex(String field, IndexOptions indexOptions);

    Completable rebuildIndex(String field, boolean async);

    Single<Collection<IndexEntry>> listIndices();

    Single<Boolean> hasIndex(String field);

    Single<Boolean> isIndexing(String field);

    Completable dropIndex(String field);

    Completable dropAllIndices();

    FlowableWriteResult insert(T[] items);

    FlowableWriteResult update(T element);

    FlowableWriteResult update(T element, boolean insertIfAbsent);

    FlowableWriteResult remove(T element);

    Completable drop();

    Single<Boolean> isDropped();

    Single<Boolean> isOpen();

    Completable close();

    Single<Long> size();

    Observable<CollectionEventInfo<?>> observe();

    Observable<CollectionEventInfo<?>> observe(EventType eventType);

    Single<Attributes> getAttributes();

    Completable setAttributes(Attributes attributes);
}
