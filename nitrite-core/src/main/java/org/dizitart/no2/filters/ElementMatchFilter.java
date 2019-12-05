package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.common.util.NumberUtils.compare;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee
 */
class ElementMatchFilter extends NitriteFilter {
    private String field;
    private org.dizitart.no2.filters.Filter elementFilter;

    ElementMatchFilter(String field, org.dizitart.no2.filters.Filter elementFilter) {
        this.elementFilter = elementFilter;
        this.field = field;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        if (elementFilter instanceof ElementMatchFilter) {
            throw new FilterException("nested elemMatch filter is not supported");
        }

        if (elementFilter instanceof org.dizitart.no2.filters.TextFilter) {
            throw new FilterException("full-text search is not supported in elemMatch filter");
        }

        Document document = element.getValue();
        Object fieldValue = document.get(field);
        if (fieldValue == null) {
            return false;
        }

        if (fieldValue.getClass().isArray()) {
            int length = Array.getLength(fieldValue);
            List list = new ArrayList(length);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(fieldValue, i);
                list.add(item);
            }

            return matches(list, elementFilter);
        } else if (fieldValue instanceof Iterable) {
            return matches((Iterable) fieldValue, elementFilter);
        } else {
            throw new FilterException("elemMatch filter only applies to array or iterable");
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean matches(Iterable iterable, org.dizitart.no2.filters.Filter filter) {
        for (Object item : iterable) {
            if (matchElement(item, filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchElement(Object item, org.dizitart.no2.filters.Filter filter) {
        if (filter instanceof org.dizitart.no2.filters.AndFilter) {
            List<org.dizitart.no2.filters.Filter> filters = ((org.dizitart.no2.filters.AndFilter) filter).getFilters();
            for (org.dizitart.no2.filters.Filter f : filters) {
                if (!matchElement(item, f)) {
                    return false;
                }
            }
            return true;
        } else if (filter instanceof org.dizitart.no2.filters.OrFilter) {
            List<org.dizitart.no2.filters.Filter> filters = ((org.dizitart.no2.filters.OrFilter) filter).getFilters();
            for (org.dizitart.no2.filters.Filter f : filters) {
                if (matchElement(item, f)) {
                    return true;
                }
            }
            return false;
        } else if (filter instanceof org.dizitart.no2.filters.NotFilter) {
            org.dizitart.no2.filters.Filter not = ((org.dizitart.no2.filters.NotFilter) filter).getFilter();
            return !matchElement(item, not);
        } else if (filter instanceof org.dizitart.no2.filters.EqualsFilter) {
            return matchEqual(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.GreaterEqualFilter) {
            return matchGreaterEqual(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.GreaterThanFilter) {
            return matchGreater(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.LesserEqualFilter) {
            return matchLesserEqual(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.LesserThanFilter) {
            return matchLesser(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.InFilter) {
            return matchIn(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.NotInFilter) {
            return matchNotIn(item, filter);
        } else if (filter instanceof org.dizitart.no2.filters.RegexFilter) {
            return matchRegex(item, filter);
        } else {
            throw new FilterException("filter " + filter.getClass() +
                " is not a supported in elemMatch");
        }
    }

    private boolean matchEqual(Object item, org.dizitart.no2.filters.Filter filter) {
        Object value = ((org.dizitart.no2.filters.EqualsFilter) filter).getValue();
        if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.EqualsFilter) filter).getField());
            return deepEquals(value, docValue);
        } else {
            return deepEquals(item, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchGreater(Object item, org.dizitart.no2.filters.Filter filter) {
        Comparable comparable = ((org.dizitart.no2.filters.GreaterThanFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) > 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) > 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.GreaterThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) > 0;
            } else {
                throw new FilterException(
                    ((org.dizitart.no2.filters.GreaterThanFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchGreaterEqual(Object item, org.dizitart.no2.filters.Filter filter) {
        Comparable comparable = ((org.dizitart.no2.filters.GreaterEqualFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) >= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) >= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.GreaterEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) >= 0;
            } else {
                throw new FilterException(((org.dizitart.no2.filters.GreaterEqualFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchLesserEqual(Object item, org.dizitart.no2.filters.Filter filter) {
        Comparable comparable = ((org.dizitart.no2.filters.LesserEqualFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) <= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) <= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.LesserEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) <= 0;
            } else {
                throw new FilterException(((org.dizitart.no2.filters.LesserEqualFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchLesser(Object item, org.dizitart.no2.filters.Filter filter) {
        Comparable comparable = ((org.dizitart.no2.filters.LesserThanFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) < 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) < 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.LesserThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) < 0;
            } else {
                throw new FilterException(((org.dizitart.no2.filters.LesserThanFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean matchIn(Object item, org.dizitart.no2.filters.Filter filter) {
        Set<Comparable> values = ((org.dizitart.no2.filters.InFilter) filter).getComparableSet();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = document.get(((org.dizitart.no2.filters.InFilter) filter).getField());
                if (docValue instanceof Comparable) {
                    return values.contains(docValue);
                }
            } else if (item instanceof Comparable) {
                return values.contains(item);
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private boolean matchNotIn(Object item, org.dizitart.no2.filters.Filter filter) {
        Set<Comparable> values = ((org.dizitart.no2.filters.NotInFilter) filter).getComparableSet();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = document.get(((org.dizitart.no2.filters.NotInFilter) filter).getField());
                if (docValue instanceof Comparable) {
                    return !values.contains(docValue);
                }
            } else if (item instanceof Comparable) {
                return !values.contains(item);
            }
        }
        return false;
    }

    private boolean matchRegex(Object item, Filter filter) {
        String value = (String)((org.dizitart.no2.filters.RegexFilter) filter).getValue();
        if (item instanceof String) {
            Pattern pattern = Pattern.compile(value);
            Matcher matcher = pattern.matcher((String) item);
            return matcher.find();
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((org.dizitart.no2.filters.RegexFilter) filter).getField());
            if (docValue instanceof String) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher((String) docValue);
                return matcher.find();
            } else {
                throw new FilterException(((org.dizitart.no2.filters.RegexFilter) filter).getField() + " is not a string");
            }
        } else {
            throw new FilterException(item + " is not a string");
        }
    }
}
