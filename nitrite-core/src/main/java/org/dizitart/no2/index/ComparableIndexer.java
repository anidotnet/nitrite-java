package org.dizitart.no2.index;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.common.util.ValidationUtils.validateDocumentIndexField;

/**
 * @author Anindya Chatterjee
 */
@SuppressWarnings("rawtypes")
public abstract class ComparableIndexer implements Indexer {
    private NitriteStore nitriteStore;
    private IndexCatalog indexCatalog;

    abstract boolean isUnique();

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = this.nitriteStore.getIndexCatalog();
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object fieldValue) {
        validate(fieldValue);
        addIndexEntry(collection.getName(), nitriteId, field, (Comparable) fieldValue);
    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object fieldValue) {
        validate(fieldValue);
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collection.getName(), field);

        Comparable element = (Comparable) fieldValue;

        // create the nitrite list associated with the value
        ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get(element);
        if (nitriteIdList != null && !nitriteIdList.isEmpty()) {
            nitriteIdList.remove(nitriteId);
            if (nitriteIdList.size() == 0) {
                indexMap.remove(element);
            } else {
                indexMap.put(element, nitriteIdList);
            }
        }
    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, Field field, Object newValue, Object oldValue) {
        validate(newValue);
        validate(oldValue);
        addIndexEntry(collection.getName(), nitriteId, field, (Comparable) newValue);
        removeIndex(collection, nitriteId, field, oldValue);
    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, Field field) {
        indexCatalog.dropIndexEntry(collection.getName(), field);
    }

    @Override
    public void rebuildIndex(NitriteMap<NitriteId, Document> collection, Field field) {
        // create index map
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collection.getName(), field);

        // remove old values
        indexMap.clear();

        for (KeyValuePair<NitriteId, Document> entry : collection.entries()) {
            // create the document
            Document object = entry.getValue();

            // retrieved the value from document
            Object fieldValue = object.get(field.getName());

            if (fieldValue == null) continue;
            validateDocumentIndexField(fieldValue, field.getName());

            // create the id list associated with the value
            ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get((Comparable) fieldValue);
            if (nitriteIdList == null) {
                nitriteIdList = new ConcurrentSkipListSet<>();
            }

            if (isUnique() && nitriteIdList.size() == 1) {
                // if key is already exists for unique type, throw error
                throw new UniqueConstraintException("unique key constraint violation for " + field);
            }

            // add the id to the list
            nitriteIdList.add(entry.getKey());
            indexMap.put((Comparable) fieldValue, nitriteIdList);
        }
    }

    public Set<NitriteId> findEqual(String collectionName, Field field, Comparable value) {
        if (value == null) return new LinkedHashSet<>();

        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        Set<NitriteId> resultSet = null;
        if (indexMap != null) {
            resultSet = indexMap.get(value);
        }

        if (resultSet == null) resultSet = new LinkedHashSet<>();
        return resultSet;
    }

    public Set<NitriteId> findGreaterThan(String collectionName, Field field, Comparable comparable) {
        validate(comparable);

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable higherKey = indexMap.higherKey(comparable);
            while (higherKey != null) {
                resultSet.addAll(indexMap.get(higherKey));
                higherKey = indexMap.higherKey(higherKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findGreaterEqual(String collectionName, Field field, Comparable comparable) {
        validate(comparable);

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable ceilingKey = indexMap.ceilingKey(comparable);
            while (ceilingKey != null) {
                resultSet.addAll(indexMap.get(ceilingKey));
                ceilingKey = indexMap.higherKey(ceilingKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findLesserThan(String collectionName, Field field, Comparable comparable) {
        validate(comparable);

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable lowerKey = indexMap.lowerKey(comparable);
            while (lowerKey != null) {
                resultSet.addAll(indexMap.get(lowerKey));
                lowerKey = indexMap.lowerKey(lowerKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findLesserEqual(String collectionName, Field field, Comparable comparable) {
        validate(comparable);

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            Comparable floorKey = indexMap.floorKey(comparable);
            while (floorKey != null) {
                resultSet.addAll(indexMap.get(floorKey));
                floorKey = indexMap.lowerKey(floorKey);
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findIn(String collectionName, Field field, Collection<Comparable> values) {
        notNull(values, "values cannot be null");

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }

    public Set<NitriteId> findNotIn(String collectionName, Field field, Collection<Comparable> values) {
        notNull(values, "values cannot be null");

        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        if (indexMap != null) {
            for (Comparable comparable : indexMap.keySet()) {
                if (!values.contains(comparable)) {
                    resultSet.addAll(indexMap.get(comparable));
                }
            }
        }

        return resultSet;
    }

    private void validate(Object value) {
        notNull(value, "value cannot be null");
        if (!(value instanceof Comparable)) {
            throw new ValidationException(value + " is not comparable");
        }
    }

    private void addIndexEntry(String collectionName, NitriteId id, Field field, Comparable element) {
        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap = getIndexMap(collectionName, field);

        // create the nitriteId list associated with the value
        ConcurrentSkipListSet<NitriteId> nitriteIdList
            = indexMap.get(element);

        if (nitriteIdList == null) {
            nitriteIdList = new ConcurrentSkipListSet<>();
        }

        if (isUnique() && nitriteIdList.size() == 1
            && !nitriteIdList.contains(id)) {
            // if key is already exists for unique type, throw error
            throw new UniqueConstraintException("unique key constraint violation for " + field);
        }

        nitriteIdList.add(id);
        indexMap.put(element, nitriteIdList);
    }

    @SuppressWarnings("rawtypes")
    private NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> getIndexMap(String collectionName, Field field) {
        String mapName = getIndexMapName(collectionName, field);
        return nitriteStore.openMap(mapName);
    }
}
