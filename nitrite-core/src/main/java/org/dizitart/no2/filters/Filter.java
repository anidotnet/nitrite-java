package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;

/**
 * An interface to specify filtering criteria during find operation. When
 * a filter is applied to a collection, based on the criteria it returns
 * a set of {@link NitriteId}s of matching records.
 *
 * Each filtering criteria is based on a value of a document. If the value
 * is indexed, the find operation takes the advantage of it and only scans
 * the index map for that value. But if the value is not indexed, it scans
 * the whole collection.
 *
 *
 * The supported filters are:
 *
 * .Comparison Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 *
 * |Equals
 * |{@link FluentFilter#eq(Object)}
 * |Matches values that are equal to a specified value.
 *
 * |Greater
 * |{@link FluentFilter#gt(Comparable)}
 * |Matches values that are greater than a specified value.
 *
 * |GreaterEquals
 * |{@link FluentFilter#gte(Comparable)}
 * |Matches values that are greater than or equal to a specified value.
 *
 * |Lesser
 * |{@link FluentFilter#lt(Comparable)}
 * |Matches values that are less than a specified value.
 *
 * |LesserEquals
 * |{@link FluentFilter#lte(Comparable)}
 * |Matches values that are less than or equal to a specified value.
 *
 * |In
 * |{@link FluentFilter#in(Comparable[])}
 * |Matches any of the values specified in an array.
 * |===
 *
 *
 * .Logical Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 *
 * |Not
 * |{@link Filter#not()}
 * |Inverts the effect of a filter and returns results that do not match the filter.
 *
 * |Or
 * |{@link Filter#or(Filter)}
 * |Joins filters with a logical OR returns all ids of the documents that match the conditions
 * of either filter.
 *
 * |And
 * |{@link Filter#and(Filter)}
 * |Joins filters with a logical AND returns all ids of the documents that match the conditions
 * of both filters.
 * |===
 *
 *
 * .Array Filter
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 *
 * |Element Match
 * |{@link FluentFilter#elemMatch(Filter)}
 * |Matches documents that contain an array field with at least one element that matches
 * the specified filter.
 * |===
 *
 *
 * .Text Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 *
 * |Text
 * |{@link FluentFilter#text(String)}
 * |Performs full-text search.
 *
 * |Regex
 * |{@link FluentFilter#regex(String)}
 * |Selects documents where values match a specified regular expression.
 * |===
 *
 *
 * [[app-listing]]
 * [source,java]
 * .Example of various filters
 * --
 *
 * // returns the ids of the documents whose age field value is 30
 * collection.find(when("age").eq(30));
 *
 * // age field value is greater than 30
 * collection.find(when("age").gt(30));
 *
 * // age field value is not 30
 * collection.find(when("age").eq(30).not()));
 *
 * // age field value is 30 and salary greater than 10K
 * collection.find(when("age").eq(30).and(when("salary").gt(10000)));
 *
 * // note field contains the string 'hello'
 * collection.find(when("note").regex("hello"));
 *
 * // prices field contains price value between 10 to 20
 * collection.find(when("prices").elemMatch($.gt(10).and($.lt(20))));
 *
 * --
 *
 * A nitrite document can contain another document. To specify a field
 * of a nested document a '.' operator is used. If a field is an array
 * or list, array/list index can be used as a field to access a specific
 * element in them.
 *
 * [[app-listing]]
 * [source,java]
 * .Example of nested document
 * --
 * NitriteMapper nitriteMapper = new JacksonMapper();
 *
 * // parse a json into a document
 * doc = nitriteMapper.parse("{" +
 * "  score: 1034," +
 * "  location: {  " +
 * "       state: 'NY', " +
 * "       city: 'New York', " +
 * "       address: {" +
 * "            line1: '40', " +
 * "            line2: 'ABC Street', " +
 * "            house: ['1', '2', '3'] " +
 * "       }" +
 * "  }," +
 * "  category: ['food', 'produce', 'grocery'], " +
 * "  objArray: [{ field: 1}, {field: 2}]" +
 * "}");
 *
 * // insert the doc into collection
 * collection.insert(doc);
 *
 * // filter on nested document
 * collection.find(when("location.address.line1").eq("40"));
 *
 * // filter on array using array index
 * collection.find(when("location.address.house.2").eq("3"));
 *
 * // filter on object array
 * collection.find(when("objArray.0.field").eq(1));
 *
 *
 * --
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#find(Filter)
 * @since 1.0
 */
public interface Filter {
    /**
     * A filter to select all elements.
     */
    Filter ALL = null;

    /**
     * Filters a document map and returns the set of {@link NitriteId}s of
     * matching {@link Document}s.
     *
     * @param element the {@link org.dizitart.no2.store.NitriteMap} entry to check.
     * @return a set of {@link NitriteId}s of matching documents.
     */
    boolean apply(KeyValuePair<NitriteId, Document> element);

    /**
     * Creates an and filter which performs a logical AND operation on two filters and selects
     * the documents that satisfy both filters.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 and
     * // 'name' field has value as John Doe
     * collection.find(when("age").eq(30).and(when("name").eq("John Doe")));
     * --
     *
     * @param filter other filter
     * @return the and filter
     */
    default Filter and(Filter filter) {
        return new AndFilter(this, filter);
    }

    /**
     * Creates an or filter which performs a logical OR operation on two filters and selects
     * the documents that satisfy at least one of the filter.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 or
     * // 'name' field has value as John Doe
     * collection.find(when("age").eq(30).or(when("name").eq("John Doe")));
     * --
     *
     * @param filter other filter
     * @return the or filter
     */
    default Filter or(Filter filter) {
        return new OrFilter(this, filter);
    }

    /**
     * Creates a not filter which performs a logical NOT operation on a `filter` and selects
     * the documents that *_do not_* satisfy the `filter`. This also includes documents
     * that do not contain the value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not equals to 30
     * collection.find(when("age").eq("age").not());
     * --
     *
     * @return the not filter
     */
    default Filter not() {
        return new NotFilter(this);
    }
}
