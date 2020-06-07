package org.dizitart.no2.rx;

import io.reactivex.Single;
import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableWriteResult extends FlowableIterable<NitriteId> {
    private final Callable<WriteResult> supplier;

    FlowableWriteResult(Callable<WriteResult> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    Single<Integer> getAffectedCount() {
        return Single.fromCallable(() -> {
            WriteResult wrapped = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return wrapped.getAffectedCount();
        });
    }
}
