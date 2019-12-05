package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteMap;

import java.text.Collator;
import java.util.*;

/**
 * @author Anindya Chatterjee.
 */
class SortedDocumentCursor implements ReadableStream<NitriteId> {
    private final String field;
    private final SortOrder sortOrder;
    private final Collator collator;
    private final NullOrder nullOrder;
    private final ReadableStream<NitriteId> readableStream;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    public SortedDocumentCursor(String field,
                                SortOrder sortOrder,
                                Collator collator,
                                NullOrder nullOrder,
                                ReadableStream<NitriteId> readableStream,
                                NitriteMap<NitriteId, Document> nitriteMap) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.collator = collator;
        this.nullOrder = nullOrder;
        this.nitriteMap = nitriteMap;
        this.readableStream = readableStream;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        return new SortedDocumentIterator(field, sortOrder, collator, nullOrder, readableStream.iterator(), nitriteMap);
    }

    static class SortedDocumentIterator implements Iterator<NitriteId> {
        private final String field;
        private final SortOrder sortOrder;
        private final Collator collator;
        private final NullOrder nullOrder;
        private final Iterator<NitriteId> iterator;
        private final NitriteMap<NitriteId, Document> nitriteMap;
        private Iterator<NitriteId> sortedIterator;

        public SortedDocumentIterator(String field,
                                      SortOrder sortOrder,
                                      Collator collator,
                                      NullOrder nullOrder,
                                      Iterator<NitriteId> iterator,
                                      NitriteMap<NitriteId, Document> nitriteMap) {
            this.field = field;
            this.sortOrder = sortOrder;
            this.collator = collator;
            this.nullOrder = nullOrder;
            this.nitriteMap = nitriteMap;
            this.iterator = iterator;
            init();
        }

        @Override
        public boolean hasNext() {
            return sortedIterator.hasNext();
        }

        @Override
        public NitriteId next() {
            return sortedIterator.next();
        }

        private void init() {
            NavigableMap<Object, List<NitriteId>> sortedMap;
            if (collator != null) {
                sortedMap = new TreeMap<>(collator);
            } else {
                sortedMap = new TreeMap<>();
            }

            Set<NitriteId> nullValueIds = new HashSet<>();
            while (iterator.hasNext()) {
                NitriteId id = iterator.next();
                Document document = nitriteMap.get(id);
                if (document == null) continue;

                Object value = document.get(field);
                if (value != null) {
                    if (value.getClass().isArray() || value instanceof Iterable) {
                        throw new InvalidOperationException("cannot sort on array or collection objects");
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
            if (sortOrder == SortOrder.Ascending) {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.First) {
                    sortedValues = new ArrayList<>(nullValueIds);
                    sortedValues.addAll(flattenList(sortedMap.values()));
                } else {
                    sortedValues = flattenList(sortedMap.values());
                    sortedValues.addAll(nullValueIds);
                }
            } else {
                if (nullOrder == NullOrder.Default || nullOrder == NullOrder.Last) {
                    sortedValues = flattenList(sortedMap.descendingMap().values());
                    sortedValues.addAll(nullValueIds);
                } else {
                    sortedValues = new ArrayList<>(nullValueIds);
                    sortedValues.addAll(flattenList(sortedMap.descendingMap().values()));
                }
            }

            this.sortedIterator = sortedValues.iterator();
        }

        private <E> List<E> flattenList(Collection<List<E>> collection) {
            List<E> finalList = new ArrayList<>();
            for (List<E> list : collection) {
                finalList.addAll(list);
            }
            return finalList;
        }
    }
}
