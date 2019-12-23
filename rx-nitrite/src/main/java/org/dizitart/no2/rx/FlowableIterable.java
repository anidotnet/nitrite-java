package org.dizitart.no2.rx;

import io.reactivex.Flowable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.operators.flowable.FlowableFromIterable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public abstract class FlowableIterable<T> extends Flowable<T> {

    private final Callable<? extends Iterable<T>> supplier;

    FlowableIterable(Callable<? extends Iterable<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        Iterator<T> it;
        try {
            Iterable<T> iterable = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            it = iterable.iterator();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, s);
            return;
        }

        FlowableFromIterable.subscribe(s, it);
    }
}
