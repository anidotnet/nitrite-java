1. Raise Indexing events (Start, End, Removed)
2. Collection Index
3. Validate indexing on null value (it should index null values)
3. All exception message must be detailed and unique as no number is there now.
4. Try implement Collection<?> for stream support
5. Old nitrite data compatibility check

ComplexType/SimpleType

if SimpleType -> mapper.writeValueType -> value
if ComplexType -> mapper.writeComplexType -> Document


Document/Object

if Document -> mapper.readDocument -> ComplexType
if SimpleType -> mapper.readValue -> SimpleType

mapper.writeValueType 
(Nitrite)
SimpleType -> SimpleType (no change)

(jackson)
SimpleType -> AnotherSimpleType (via module)

mapper.writeComplexType
(Nitrite)
ComplexType -> TypeConverter/Mappable -> Document

(jackson)
ComplexType -> ObjectMapper -> Document

mapper.readDocument
(Nitrite)
Document -> TypeConverter/Mappable -> ComplexType

(jackson)
Document -> ObjectMapper -> ComplexType

mapper.readValue
(Nitrite)
SimpleType -> SimpleType (no change)

(jackson)
AnotherSimpleType -> SimpleType (via module)



=====================================

{
	first: "value",
	seconds: ["1", "2"],
	third: null,
	fourth: {
		first: "value",
		second: ["1", "2"],
		third: {
			first: [1, 2],
			second: "other"
		}
	},
	fifth: [
		{
			first: "value",
			second: [1, 2, 3],
			third: {
				first: "value",
				second: [1, 2]
			},
			fourth: [
				{
					first: "value",
					second: [1, 2]
				},
				{
					first: "value",
					second: [1, 2]
				}
			]
		},
		{
			first: "value",
			second: [1, 2, 3],
			third: {
				first: "value",
				second: [1, 2]
			},
			fourth: [
				{
					first: "value",
					second: [1, 2]
				},
				{
					first: "value",
					second: [1, 2]
				}
			]
		},
		{
			first: "value",
			second: [1, 2, 3],
			third: {
				first: "value",
				second: [1, 2]
			},
			fourth: [
				{
					first: "value",
					second: [1, 2]
				},
				{
					first: "value",
					second: [1, 2]
				}
			]
		}
	]
}



---------------------------


generic accessor, specific accessor

use case for generic accessor
1. Create index on array field - pattern
fifth.second
fifth.fourth.second

2. Create index on object field - pattern
fourth.third.second


use case for specific accesor
1. Get value from an array field - exact value
fifth.0.second.0
fifth.1.fourth.0.second.1

2. Get value from an object field - exact value
fourth.third.second

Steps.
1. Split by separator
2. if primitive value -> return
3. if object but not document
	1. if comparable return
	2. throw exception
4. if document
	1. access the field get value and goto Step 2.
5. if array/iterable
	1. if accessor is integer, check range, get value at index and goto step 2.
	2. if accessor not integer, get array and goto step 2.
