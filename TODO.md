1. Raise Indexing events (Start, End, Removed)
2. Collection Index
3. Validate indexing on null value (it should index null values)
3. All exception message must be detailed and unique as no number is there now.

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