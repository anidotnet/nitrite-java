package org.dizitart.no2.collection.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
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
 * |{@link Filter#eq(String, Object)}
 * |Matches values that are equal to a specified value.
 *
 * |Greater
 * |{@link Filter#gt(String, Comparable)}
 * |Matches values that are greater than a specified value.
 *
 * |GreaterEquals
 * |{@link Filter#gte(String, Comparable)}
 * |Matches values that are greater than or equal to a specified value.
 *
 * |Lesser
 * |{@link Filter#lt(String, Comparable)}
 * |Matches values that are less than a specified value.
 *
 * |LesserEquals
 * |{@link Filter#lte(String, Comparable)}
 * |Matches values that are less than or equal to a specified value.
 *
 * |In
 * |{@link Filter#in(String, Comparable[])}
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
 * |{@link Filter#not(Filter)}
 * |Inverts the effect of a filter and returns results that do not match the filter.
 *
 * |Or
 * |{@link Filter#or(Filter[])}
 * |Joins filters with a logical OR returns all ids of the documents that match the conditions
 * of either filter.
 *
 * |And
 * |{@link Filter#and(Filter[])}
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
 * |{@link Filter#elemMatch(String, Filter)}
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
 * |{@link Filter#text(String, String)}
 * |Performs full-text search.
 *
 * |Regex
 * |{@link Filter#regex(String, String)}
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
 * collection.find(eq("age", 30));
 *
 * // age field value is greater than 30
 * collection.find(gt("age", 30));
 *
 * // age field value is not 30
 * collection.find(not(eq("age", 30)));
 *
 * // age field value is 30 and salary greater than 10K
 * collection.find(and(eq("age", 30), gt("salary", 10000)));
 *
 * // note field contains the string 'hello'
 * collection.find(regex("note", "hello"));
 *
 * // prices field contains price value between 10 to 20
 * collection.find(elemMatch("prices", and(gt("$", 10), lt("$", 20))));
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
 * collection.find(eq("location.address.line1", "40"));
 *
 * // filter on array using array index
 * collection.find(eq("location.address.house.2", "3"));
 *
 * // filter on object array
 * collection.find(eq("objArray.0.field", 1));
 *
 *
 * --
 *
 * @author Anindya Chatterjee
 * @see org.dizitart.no2.collection.NitriteCollection#find(Filter, org.dizitart.no2.collection.FindOptions)
 * @see org.dizitart.no2.collection.NitriteCollection#find(Filter)
 * @since 1.0
 */
public interface Filter {

    /**
     * Filters a document map and returns the set of {@link NitriteId}s of
     * matching {@link Document}s.
     *
     * @param nitriteMap the document map
     * @return a set of {@link NitriteId}s of matching documents.
     */
    boolean apply(KeyValuePair<NitriteId, Document> element);
}
