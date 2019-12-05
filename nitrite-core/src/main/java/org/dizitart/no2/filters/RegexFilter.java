package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anindya Chatterjee
 */
class RegexFilter extends FieldBasedFilter {
    RegexFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        String value = (String) getValue();
        Pattern pattern = Pattern.compile(value);

        Document document = element.getValue();
        Object fieldValue = document.get(getField());
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                Matcher matcher = pattern.matcher((String) fieldValue);
                if (matcher.find()) {
                    return true;
                }
                matcher.reset();
            } else {
                throw new FilterException(getField() + " does not contain string value.");
            }
        }
        return false;
    }
}
