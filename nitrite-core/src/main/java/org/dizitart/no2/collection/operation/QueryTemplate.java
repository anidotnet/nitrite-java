package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NullOrder;
import org.dizitart.no2.collection.SortOrder;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.index.IndexedQueryTemplate;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

import java.text.Collator;
import java.util.*;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.exceptions.ErrorMessage.*;

/**
 * @author Anindya Chatterjee
 */
class QueryTemplate {
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMap<NitriteId, Document> nitriteMap;

    QueryTemplate(IndexTemplate indexTemplate,
                  NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;
        this.indexedQueryTemplate = new NitriteIndexedQueryTemplate(indexTemplate);
    }

    public DocumentCursor find() {
        FindResult findResult = new FindResult();
        findResult.setHasMore(false);
        findResult.setTotalCount(nitriteMap.size());
        findResult.setIdSet(nitriteMap.keySet());
        findResult.setNitriteMap(nitriteMap);

        return new DocumentCursorImpl(findResult);
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null) {
            return find();
        }
        filter.setIndexedQueryTemplate(indexedQueryTemplate);
        ReadableStream<NitriteId> result;

        try {
            result = filter.apply(nitriteMap);
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(FILTERED_FIND_OPERATION_FAILED, t);
        }

        FindResult findResult = new FindResult();
        findResult.setNitriteMap(nitriteMap);
        if (result != null) {
            findResult.setHasMore(false);
            findResult.setTotalCount(result.size());
            findResult.setIdSet(result);
        }

        return new DocumentCursorImpl(findResult);
    }

    public DocumentCursor find(FindOptions findOptions) {
        FindResult findResult = new FindResult();
        findResult.setNitriteMap(nitriteMap);
        setUnfilteredResultSet(findOptions, findResult);

        return new DocumentCursorImpl(findResult);
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        if (filter == null) {
            return find(findOptions);
        }
        filter.setIndexedQueryTemplate(indexedQueryTemplate);
        FindResult findResult = new FindResult();
        findResult.setNitriteMap(nitriteMap);
        setFilteredResultSet(filter, findOptions, findResult);

        return new DocumentCursorImpl(findResult);
    }

    Document getById(NitriteId nitriteId) {
        return nitriteMap.get(nitriteId);
    }

    private void setUnfilteredResultSet(FindOptions findOptions, FindResult findResult) {
        validateLimit(findOptions, nitriteMap.size());

        ReadableStream<NitriteId> resultSet;
        if (isNullOrEmpty(findOptions.getField())) {
            resultSet = limitIdSet(nitriteMap.keySet(), findOptions);
        } else {
            resultSet = sortIdSet(nitriteMap.keySet(), findOptions);
        }

        findResult.setIdSet(resultSet);
        findResult.setTotalCount(nitriteMap.size());
        findResult.setHasMore(nitriteMap.keySet().size() > (findOptions.getSize() + findOptions.getOffset()));
    }

    private void setFilteredResultSet(Filter filter, FindOptions findOptions, FindResult findResult) {
        ReadableStream<NitriteId> nitriteIdSet;
        try {
            nitriteIdSet = filter.apply(nitriteMap);
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(FILTERED_FIND_WITH_OPTIONS_OPERATION_FAILED, t);
        }

        if (nitriteIdSet == null || nitriteIdSet.isEmpty()) return;

        validateLimit(findOptions, nitriteIdSet.size());
        ReadableStream<NitriteId> resultSet;

        if (isNullOrEmpty(findOptions.getField())) {
            resultSet = limitIdSet(nitriteIdSet, findOptions);
        } else {
            resultSet = sortIdSet(nitriteIdSet, findOptions);
        }

        findResult.setIdSet(resultSet);
        findResult.setHasMore(nitriteIdSet.size() > (findOptions.getSize() + findOptions.getOffset()));
        findResult.setTotalCount(nitriteIdSet.size());
    }

    private ReadableStream<NitriteId> sortIdSet(ReadableStream<NitriteId> nitriteIdSet, FindOptions findOptions) {
        String sortField = findOptions.getField();
        Collator collator = findOptions.getCollator();

        NavigableMap<Object, List<NitriteId>> sortedMap;
        if (collator != null) {
            sortedMap = new TreeMap<>(collator);
        } else {
            sortedMap = new TreeMap<>();
        }

        Set<NitriteId> nullValueIds = new HashSet<>();

        for (NitriteId id : nitriteIdSet) {
            Document document = nitriteMap.get(id);
            if (document == null) continue;

            Object value = document.get(sortField);

            if (value != null) {
                if (value.getClass().isArray() || value instanceof Iterable) {
                    throw new InvalidOperationException(UNABLE_TO_SORT_ON_ARRAY);
                }
            } else {
                nullValueIds.add(id);
                continue;
            }

            if (sortedMap.containsKey(value)) {
                List<NitriteId> idList = sortedMap.get(value);
                idList.add(id);
                sortedMap.put(value, idList);
            } else {
                List<NitriteId> idList = new ArrayList<>();
                idList.add(id);
                sortedMap.put(value, idList);
            }
        }

        List<NitriteId> sortedValues;
        if (findOptions.getSortOrder() == SortOrder.Ascending) {
            if (findOptions.getNullOrder() == NullOrder.Default || findOptions.getNullOrder() == NullOrder.First) {
                sortedValues = new ArrayList<>(nullValueIds);
                sortedValues.addAll(flattenList(sortedMap.values()));
            } else {
                sortedValues = flattenList(sortedMap.values());
                sortedValues.addAll(nullValueIds);
            }
        } else {
            if (findOptions.getNullOrder() == NullOrder.Default || findOptions.getNullOrder() == NullOrder.Last) {
                sortedValues = flattenList(sortedMap.descendingMap().values());
                sortedValues.addAll(nullValueIds);
            } else {
                sortedValues = new ArrayList<>(nullValueIds);
                sortedValues.addAll(flattenList(sortedMap.descendingMap().values()));
            }
        }

        return limitIdSet(ReadableStream.fromIterable(sortedValues), findOptions);
    }

    private ReadableStream<NitriteId> limitIdSet(ReadableStream<NitriteId> nitriteIdSet, FindOptions findOptions) {
        int offset = findOptions.getOffset();
        int size = findOptions.getSize();
        Set<NitriteId> resultSet = new LinkedHashSet<>();

        int index = 0;
        for (NitriteId nitriteId : nitriteIdSet) {
            if (index >= offset) {
                resultSet.add(nitriteId);
                if (index == (offset + size - 1)) break;
            }
            index++;
        }

        return ReadableStream.fromIterable(resultSet);
    }

    private <T> List<T> flattenList(Collection<List<T>> collection) {
        List<T> finalList = new ArrayList<>();
        for (List<T> list : collection) {
            finalList.addAll(list);
        }
        return finalList;
    }

    private void validateLimit(FindOptions findOptions, long totalSize) {
        if (findOptions.getSize() < 0) {
            throw new ValidationException(PAGINATION_SIZE_CAN_NOT_BE_NEGATIVE);
        }

        if (findOptions.getOffset() < 0) {
            throw new ValidationException(PAGINATION_OFFSET_CAN_NOT_BE_NEGATIVE);
        }

        if (totalSize < findOptions.getOffset()) {
            throw new ValidationException(PAGINATION_OFFSET_GREATER_THAN_SIZE);
        }
    }
}
