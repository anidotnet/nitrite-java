package org.dizitart.no2.module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public interface NitriteModule {
    static NitriteModule module(NitritePlugin... plugins) {
        return new NitriteModule() {
            @Override
            public Set<NitritePlugin> plugins() {
                return setOf(plugins);
            }
        };
    }

    Set<NitritePlugin> plugins();

    @SuppressWarnings("unchecked")
    default <T> Set<T> setOf(T... items) {
        Set<T> set = new HashSet<>();
        if (items != null) {
            set.addAll(Arrays.asList(items));
        }
        return set;
    }
}
