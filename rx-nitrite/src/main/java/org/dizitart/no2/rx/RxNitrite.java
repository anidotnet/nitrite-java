package org.dizitart.no2.rx;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class RxNitrite {
    private final Nitrite nitrite;
    private final NitriteConfig nitriteContext;

    RxNitrite(Nitrite nitrite) {
        this.nitrite = nitrite;
        this.nitriteContext = nitrite.getConfig();
    }

    public RxNitriteCollection getCollection(String name) {
        NitriteCollection collection = nitrite.getCollection(name);
        return new RxNitriteCollectionImpl(collection);
    }

    public <T> RxObjectRepository<T> getRepository(Class<T> type) {
        ObjectRepository<T> repository = nitrite.getRepository(type);
        return new RxObjectRepositoryImpl<>(repository, nitriteContext);
    }

    public <T> RxObjectRepository<T> getRepository(String key, Class<T> type) {
        ObjectRepository<T> repository = nitrite.getRepository(type, key);
        return new RxObjectRepositoryImpl<>(repository, nitriteContext);
    }

    public Single<Set<String>> listRepositories() {
        return Single.fromCallable(nitrite::listRepositories);
    }

    public Single<Map<String, Set<String>>> listKeyedRepository() {
        return Single.fromCallable(nitrite::listKeyedRepository);
    }

    public Single<Boolean> hasCollection(String name) {
        return Single.fromCallable(() -> nitrite.hasCollection(name));
    }

    public <T> Single<Boolean> hasRepository(Class<T> type) {
        return Single.fromCallable(() -> nitrite.hasRepository(type));
    }

    public <T> Single<Boolean> hasRepository(Class<T> type, String key) {
        return Single.fromCallable(() -> nitrite.hasRepository(type, key));
    }

    public Single<Boolean> hasUnsavedChanges() {
        return Single.fromCallable(nitrite::hasUnsavedChanges);
    }

    public Completable commit() {
        return Completable.fromAction(nitrite::commit);
    }

    public Single<Boolean> isClosed() {
        return Single.fromCallable(nitrite::isClosed);
    }

    public Completable close() {
        return Completable.fromAction(() -> {
            if (!nitrite.isClosed()) {
                nitrite.close();
            }
        });
    }
}
