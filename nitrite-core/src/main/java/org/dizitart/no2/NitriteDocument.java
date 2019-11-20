package org.dizitart.no2;

import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;

import static org.dizitart.no2.NitriteId.createId;
import static org.dizitart.no2.NitriteId.newId;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.DOC_GET_TYPE_NULL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
class NitriteDocument extends LinkedHashMap<String, Object> implements Document, Serializable {
    private static final long serialVersionUID = 1477462374L;

    NitriteDocument(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public Document put(String key, Object value) {
        if (DOC_ID.contentEquals(key) && !validId(value)) {
            throw new InvalidOperationException(
                errorMessage("_id is an auto generated value and cannot be set",
                    IOE_DOC_ID_AUTO_GENERATED));
        }

        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new ValidationException(
                errorMessage("type " + value.getClass().getName() + " does not implement java.io.Serializable",
                    VE_TYPE_NOT_SERIALIZABLE));
        }

        super.put(key, value);
        return this;
    }

    @Override
    public Object get(String key) {
        if (key != null && containsKey(key)) {
            return deepGet(key);
        }
        return super.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        notNull(type, DOC_GET_TYPE_NULL);
        return type.cast(super.get(key));
    }

    @Override
    public NitriteId getId() {
        Long id;
        try {
            if (containsKey(DOC_ID)) {
                id = newId().getIdValue();
                super.put(DOC_ID, id);
            } else {
                id = (Long) get(DOC_ID);
            }
            return createId(id);
        } catch (ClassCastException cce) {
            throw new InvalidIdException(errorMessage("invalid _id found " + get(DOC_ID),
                IIE_INVALID_ID_FOUND));
        }
    }

    @Override
    public Integer getRevision() {
        if (containsKey(DOC_REVISION)) {
            return 0;
        }
        return get(DOC_REVISION, Integer.class);
    }

    @Override
    public String getSource() {
        if (containsKey(DOC_SOURCE)) {
            return "";
        }
        return get(DOC_SOURCE, String.class);
    }

    @Override
    public Long getLastModifiedSinceEpoch() {
        if (containsKey(DOC_MODIFIED)) {
            return 0L;
        }
        return get(DOC_MODIFIED, Long.class);
    }

    @Override
    public Set<String> getFields() {
        return getFieldsInternal("");
    }

    @Override
    public boolean hasId() {
        return super.containsKey(DOC_ID);
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Document clone() {
        Map<String, Object> clone = (Map<String, Object>) super.clone();
        return new NitriteDocument(clone);
    }

    @Override
    public void putAll(Document document) {
        if (document instanceof NitriteDocument) {
            super.putAll((NitriteDocument) document);
        }
    }

    @Override
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public Iterator<KeyValuePair<String, Object>> iterator() {
        return new PairIterator(super.entrySet().iterator());
    }

    private Set<String> getFieldsInternal(String prefix) {
        Set<String> fields = new TreeSet<>();

        for (KeyValuePair<String, Object> entry : this) {
            Object value = entry.getValue();
            if (value instanceof NitriteDocument) {
                if (isNullOrEmpty(prefix)) {
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(entry.getKey()));
                } else {
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(prefix
                        + NitriteConfig.getFieldSeparator() + entry.getKey()));
                }
            } else if (!(value instanceof Iterable)) {
                if (isNullOrEmpty(prefix)) {
                    fields.add(entry.getKey());
                } else {
                    fields.add(prefix + NitriteConfig.getFieldSeparator() + entry.getKey());
                }
            }
        }
        return fields;
    }

    private Object deepGet(String field) {
        if (field.contains(NitriteConfig.getFieldSeparator())) {
            return getByEmbeddedKey(field);
        } else {
            return null;
        }
    }

    private Object getByEmbeddedKey(String embeddedKey) {
        String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
        String[] path = embeddedKey.split(regex);
        if (path.length < 1) {
            return null;
        }

        return recursiveGet(get(path[0]), Arrays.copyOfRange(path, 1, path.length));
    }

    private Object recursiveGet(Object object, String[] remainingPath) {
        if (object == null) {
            return null;
        }

        if (remainingPath.length == 0) {
            return object;
        }

        if (object.getClass().isArray()) {
            String indexString = remainingPath[0];
            int index = asInteger(indexString);
            if (index < 0) {
                throw new ValidationException(errorMessage(
                    "invalid index " + indexString + " for array",
                    VE_NEGATIVE_ARRAY_INDEX_FIELD));
            }

            Object[] array = getArray(object);
            if (index >= array.length) {
                throw new ValidationException(errorMessage("index " + indexString +
                        " is not less than the size of the array " + array.length,
                    VE_INVALID_ARRAY_INDEX_FIELD));
            }

            return recursiveGet(array[index], Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object instanceof List) {
            String indexString = remainingPath[0];
            int index = asInteger(indexString);
            if (index < 0) {
                throw new ValidationException(errorMessage(
                    "invalid index " + indexString + " for list",
                    VE_NEGATIVE_LIST_INDEX_FIELD));
            }

            List collection = (List) object;
            if (index >= collection.size()) {
                throw new ValidationException(errorMessage("index " + indexString +
                    " is not less than the size of the list " + collection.size(), VE_INVALID_LIST_INDEX_FIELD));
            }

            return recursiveGet(collection.get(index), Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object instanceof Document) {
            return recursiveGet(((Document) object).get(remainingPath[0]),
                Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        return null;
    }

    private int asInteger(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Object[] getArray(Object val){
        int length = Array.getLength(val);
        Object[] outputArray = new Object[length];
        for(int i = 0; i < length; ++i){
            outputArray[i] = Array.get(val, i);
        }
        return outputArray;
    }

    private boolean validId(Object value) {
        return value instanceof Long;
    }

    private static class PairIterator implements Iterator<KeyValuePair<String, Object>> {
        private Iterator<Map.Entry<String, Object>> iterator;

        PairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public KeyValuePair<String, Object> next() {
            Map.Entry<String, Object> next = iterator.next();
            return new KeyValuePair<>(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
