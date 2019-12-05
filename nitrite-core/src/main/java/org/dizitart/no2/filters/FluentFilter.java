package org.dizitart.no2.filters;

import org.dizitart.no2.index.TextIndexer;

/**
 * @author Anindya Chatterjee.
 */
public final class FluentFilter {
    public static FluentFilter $ = when("$");
    private String field;

    private FluentFilter() {
    }

    public static FluentFilter when(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates an equality filter which matches documents where the value
     * of a field equals the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30
     * collection.find(when("age").eq(30));
     * --
     *
     * @param value the value
     * @return the equality filter.
     */
    public Filter eq(Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates a greater than filter which matches those documents where the value
     * of the value is greater than (i.e. >) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than 30
     * collection.find(when("age").gt(30));
     * --
     *
     * @param value the value
     * @return the greater than filter
     */
    public Filter gt(Comparable<?> value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those documents where the value
     * of the value is greater than or equals to (i.e. >=) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than or equal to 30
     * collection.find(when("age").gte(30));
     * --
     *
     * @param value the value
     * @return the greater or equal filter
     */
    public Filter gte(Comparable<?> value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those documents where the value
     * of the value is less than (i.e. <) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value less than 30
     * collection.find(when("age").lt(30));
     * --
     *
     * @param value the value
     * @return the lesser than filter
     */
    public Filter lt(Comparable<?> value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those documents where the value
     * of the value is lesser than or equals to (i.e. <=) the specified value.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value lesser than or equal to 30
     * collection.find(when("age").lte(30));
     * --
     *
     * @param value the value
     * @return the lesser equal filter
     */
    public Filter lte(Comparable<?> value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'address' field has value 'roads'.
     * collection.find(when("address").text("roads"));
     * --
     *
     * @param value the text value
     * @return the text filter
     * @see TextIndexer
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    public Filter text(String value) {
        return new TextFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in documents.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'name' value starts with 'jim' or 'joe'.
     * collection.find(when("address").regex("^(jim|joe).*"));
     * --
     *
     * @param value the regular expression
     * @return the regex filter
     */
    public Filter regex(String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates an in filter which matches the documents where
     * the value of a field equals any value in the specified array.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value in [20, 30, 40]
     * collection.find(when("age").in(20, 30, 40));
     * --
     *
     * @param values the range values
     * @return the in filter
     */
    public Filter in(Comparable<?>... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates a notIn filter which matches the documents where
     * the value of a field not equals any value in the specified array.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not in [20, 30, 40]
     * collection.find(when("age").notIn(20, 30, 40));
     * --
     *
     * @param values the range values
     * @return the notIn filter
     */
    public Filter notIn(Comparable<?>... values) {
        return new NotInFilter(field, values);
    }

    /**
     * Creates an element match filter that matches documents that contain an array
     * value with at least one element that matches the specified `filter`.
     * <p>
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents which has an array field - 'color' and the array
     * // contains a value - 'red'.
     * collection.find(when("age").elemMatch($.eq("red")));
     * --
     *
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    public Filter elemMatch(Filter filter) {
        return new ElementMatchFilter(field, filter);
    }
}
