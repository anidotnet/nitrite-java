package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public abstract class NitriteFilter implements Filter {
    private NitriteConfig nitriteConfig;
    private String collectionName;
    private Boolean objectFilter = false;

    @SuppressWarnings("rawtypes")
    protected Set<Comparable> convertValues(Set<Comparable> values) {
        if (objectFilter) {
            NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
            Set<Comparable> convertedValues = new HashSet<>();

            for (Comparable comparable : values) {
                if (comparable == null
                    || !nitriteMapper.isValueType(comparable)) {
                    throw new FilterException("search term " + comparable
                        + " is not a comparable");
                }

                if (nitriteMapper.isValueType(comparable)) {
                    Comparable convertValue = (Comparable) nitriteMapper.convertValue(comparable);
                    convertedValues.add(convertValue);
                }
            }

            return convertedValues;
        }
        return values;
    }
}
