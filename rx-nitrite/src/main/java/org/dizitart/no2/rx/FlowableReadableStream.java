package org.dizitart.no2.rx;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.common.ReadableStream;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public abstract class FlowableReadableStream<T> extends FlowableIterable<T> {

    private final Callable<? extends ReadableStream<T>> supplier;

    static <R> FlowableReadableStream<R> create(Callable<? extends ReadableStream<R>> supplier) {
        return new FlowableReadableStream<R>(supplier) {
        };
    }

    FlowableReadableStream(Callable<? extends ReadableStream<T>> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public Single<Long> size() {
        return Single.fromCallable(() -> {
            ReadableStream<T> recordIterable = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return recordIterable.size();
        });
    }

    public Maybe<T> firstOrNull() {
        return this.firstElement();
    }
}
