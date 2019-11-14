/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.exceptions;

/**
 * Nitrite error codes
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class ErrorCodes {

    // region ValidationException Codes

    static final int VE_NEGATIVE_PAGINATION_SIZE = 1001;
    static final int VE_NEGATIVE_PAGINATION_OFFSET = 1002;
    static final int VE_OFFSET_GREATER_THAN_SIZE = 1003;
    static final int VE_REPOSITORY_NOT_INITIALIZED = 1004;
    static final int VE_PROJECTION_PRIMITIVE_TYPE = 1005;
    static final int VE_PROJECTION_INTERFACE = 1006;
    static final int VE_PROJECTION_ARRAY = 1007;
    static final int VE_PROJECTION_ABSTRACT_TYPE = 1008;
    static final int VE_PROJECT_EMPTY_TYPE = 1009;
    static final int VE_SYNC_NO_REMOTE = 1010;
    public static final int VE_INDEX_NULL_INDEX_TYPE = 1011;
    public static final int VE_INDEX_NULL_FIELD = 1012;
    public static final int VE_INDEX_EMPTY_FIELD = 1013;
    public static final int VE_INDEX_NULL_COLLECTION = 1014;
    public static final int VE_INDEX_EMPTY_COLLECTION = 1015;
    public static final int VE_COLLECTION_NULL_NAME = 1016;
    public static final int VE_COLLECTION_EMPTY_NAME = 1017;
    public static final int VE_COLLECTION_NAME_RESERVED = 1018;
    public static final int VE_SEARCH_TERM_NULL_FIELD = 1019;
    public static final int VE_SEARCH_TERM_EMPTY_FIELD = 1020;
    public static final int VE_IN_FILTER_NULL_FIELD = 1021;
    public static final int VE_IN_FILTER_EMPTY_FIELD = 1022;
    public static final int VE_IN_FILTER_NULL_VALUES = 1023;
    public static final int VE_IN_FILTER_EMPTY_VALUES = 1024;
    public static final int VE_REFLECT_NULL_START_CLASS = 1025;
    public static final int VE_REFLECT_NULL_ANNOTATION_CLASS = 1026;
    public static final int VE_REFLECT_FIELD_NULL_START_CLASS = 1027;
    public static final int VE_REFLECT_FIELD_NO_SUCH_FIELD = 1028;
    public static final int VE_OBJ_STORE_NULL_TYPE = 1029;
    public static final int VE_INDEX_ANNOTATION_NULL_TYPE = 1030;
    public static final int VE_MAP_STORE_NULL = 1031;
    public static final int VE_IS_INDEXING_NULL_FIELD = 1032;
    public static final int VE_HAS_INDEX_NULL_FIELD = 1033;
    public static final int VE_FIND_EQUAL_INDEX_NULL_FIELD = 1034;
    public static final int VE_FIND_GT_INDEX_NULL_FIELD = 1035;
    public static final int VE_FIND_GT_INDEX_NULL_VALUE = 1036;
    public static final int VE_FIND_GTE_INDEX_NULL_FIELD = 1037;
    public static final int VE_FIND_GTE_INDEX_NULL_VALUE = 1038;
    public static final int VE_FIND_LT_INDEX_NULL_FIELD = 1039;
    public static final int VE_FIND_LT_INDEX_NULL_VALUE = 1040;
    public static final int VE_FIND_LTE_INDEX_NULL_FIELD = 1041;
    public static final int VE_FIND_LTE_INDEX_NULL_VALUE = 1042;
    public static final int VE_FIND_IN_INDEX_NULL_FIELD = 1043;
    public static final int VE_FIND_IN_INDEX_NULL_VALUE = 1044;
    public static final int VE_FIND_TEXT_INDEX_NULL_FIELD = 1045;
    public static final int VE_FIND_TEXT_INDEX_NULL_VALUE = 1046;
    public static final int VE_CREATE_INDEX_NULL_FIELD = 1047;
    public static final int VE_REBUILD_INDEX_NULL_FIELD = 1048;
    public static final int VE_ATTRIBUTE_NULL = 1049;
    public static final int VE_LISTENER_DEREGISTER_NULL = 1050;
    public static final int VE_DROP_INDEX_NULL_FIELD = 1051;
    public static final int VE_INSERT_NULL_DOCUMENT = 1052;
    public static final int VE_INSERT_NULL_DOCUMENT_ARRAY = 1053;
    public static final int VE_FIND_NULL_FIND_OPTIONS = 1054;
    public static final int VE_FIND_FILTERED_NULL_FIND_OPTIONS = 1055;
    public static final int VE_GET_BY_ID_NULL_ID = 1056;
    public static final int VE_UPDATE_NULL_DOCUMENT = 1057;
    public static final int VE_UPDATE_NULL_UPDATE_OPTIONS = 1058;
    public static final int VE_INDEXED_QUERY_TEMPLATE_NULL = 1059;
    public static final int VE_SYNC_NULL_COLLECTION = 1060;
    public static final int VE_RECOVER_NULL_FILE_NAME = 1061;
    public static final int VE_RECOVER_EMPTY_FILE_NAME = 1062;
    public static final int VE_RECOVER_NULL_WRITER = 1063;
    public static final int VE_REMOVE_NULL_DOCUMENT = 1064;
    public static final int VE_PROJECT_NULL_PROJECTION = 1065;
    public static final int VE_OBJ_CREATE_INDEX_NULL_FIELD = 1066;
    public static final int VE_OBJ_UPDATE_NULL_OBJECT = 1067;
    public static final int VE_OBJ_UPDATE_NULL_DOCUMENT = 1068;
    public static final int VE_NC_REBUILD_INDEX_NULL_INDEX = 1069;
    static final int VE_OBJ_INVALID_EMBEDDED_FIELD = 1070;
    public static final int VE_OBJ_INVALID_FIELD = 1071;
    public static final int VE_BIG_DECIMAL_INVALID_FORMAT = 1072;
    static final int VE_INVALID_EMBEDDED_FIELD = 1073;
    public static final int VE_NEGATIVE_LIST_INDEX_FIELD = 1074;
    public static final int VE_INVALID_LIST_INDEX_FIELD = 1075;
    public static final int VE_NEGATIVE_ARRAY_INDEX_FIELD = 1076;
    public static final int VE_INVALID_ARRAY_INDEX_FIELD = 1077;
    public static final int VE_NITRITE_CONTEXT_NULL = 1078;
    public static final int VE_TYPE_NOT_SERIALIZABLE = 1079;
    static final int VE_PROJECTION_WITH_NOT_NULL_VALUE = 1080;
    public static final int VE_OBJ_STORE_NULL_KEY = 1081;
    public static final int VE_OBJ_STORE_EMPTY_KEY = 1082;
    public static final int VE_INVALID_KEYED_OBJ_STORE_KEY = 1083;
    public static final int VE_INVALID_KEYED_OBJ_STORE_TYPE = 1084;
    public static final int VE_CONTEXT_NULL = 1085;
    public static final int VE_OBJ_REBUILD_INDEX_NULL_FIELD = 1086;
    public static final int VE_OBJ_HAS_INDEX_NULL_FIELD = 1087;
    public static final int VE_OBJ_IS_INDEXING_NULL_FIELD = 1088;
    public static final int VE_OBJ_DROP_INDEX_NULL_FIELD = 1089;
    public static final int VE_OBJ_INSERT_NULL = 1090;
    public static final int VE_OBJ_INSERT_ARRAY_NULL = 1091;
    public static final int VE_OBJ_INSERT_ARRAY_CONTAINS_NULL = 1092;
    public static final int VE_OBJ_UPDATE_NULL = 1093;
    public static final int VE_OBJ_UPDATE_ELEMENT_NULL = 1094;
    public static final int VE_OBJ_FILTER_UPDATE_NULL = 1095;
    public static final int VE_OBJ_FILTER_UPDATE_NULL_DOCUMENT = 1096;
    public static final int VE_OBJ_REMOVE_NULL_OBJECT = 1097;
    public static final int VE_OBJ_REMOVE_OPTION_NULL = 1098;
    public static final int VE_OBJ_FIND_OPTION_NULL = 1099;
    public static final int VE_OBJ_FILTER_FIND_OPTION_NULL = 1100;
    public static final int VE_OBJ_NITRITE_ID_GET_NULL = 1101;
    public static final int VE_OBJ_ATTRIBUTE_NULL = 1102;
    public static final int VE_OBJ_LISTENER_NULL = 1103;
    public static final int VE_OBJ_LISTENER_DEREGISTER_NULL = 1104;
    public static final int VE_OBJ_INSERT_OTHERS_CONTAINS_NULL = 1105;
    public static final int VE_INSERT_OTHERS_CONTAINS_NULL = 1106;
    public static final int VE_INSERT_DOCUMENTS_CONTAINS_NULL = 1107;
    public static final int VE_UPDATE_NULL_DOCUMENT_OPTION = 1108;
    public static final int VE_UPDATE_NULL_DOCUMENT_FILTER = 1109;
    public static final int VE_UPDATE_OPTIONS_NULL_DOCUMENT = 1110;
    public static final int VE_LISTENER_NULL = 1111;
    public static final int VE_TYPE_NULL = 1112;
    public static final int VE_COLLECTION_NULL = 1113;
    public static final int VE_FIND_NOT_IN_INDEX_NULL_FIELD = 1114;
    public static final int VE_FIND_NOT_IN_INDEX_NULL_VALUE = 1115;
    public static final int VE_NOT_IN_FILTER_NULL_FIELD = 1116;
    public static final int VE_NOT_IN_FILTER_EMPTY_FIELD = 1117;
    public static final int VE_NOT_IN_FILTER_NULL_VALUES = 1118;
    public static final int VE_NOT_IN_FILTER_EMPTY_VALUES = 1119;

    // endregion

    // region NitriteIOException Codes

    static final int NIOE_DATABASE_OPENED = 2001;
    static final int NIOE_REPAIR_FAILED = 2002;
    static final int NIOE_IN_MEMORY_FAILED = 2003;
    static final int NIOE_STORE_CLOSED = 2004;
    static final int NIOE_COLLECTION_DROPPED = 2005;
    public static final int NIOE_IMPORT_ERROR = 2006;
    static final int NIOE_IMPORT_READER_ERROR = 2007;
    static final int NIOE_IMPORT_READ_ERROR = 2008;
    public static final int NIOE_EXPORT_ERROR = 2009;
    static final int NIOE_EXPORT_WRITER_ERROR = 2010;
    static final int NIOE_EXPORT_WRITE_ERROR = 2011;
    public static final int NIOE_DIR_DOES_NOT_EXISTS = 2012;
    static final int NIOE_FILE_CREATE_FAILED = 2013;
    static final int NIOE_FAILED_CREATE_READONLY_DB = 2014;
    static final int NIOE_NITRITE_STORE_CLOSED = 2015;
    public static final int NIOE_CLOSED_FAILED = 2016;
    public static final int NIOE_CLOSED_NON_W_CHANNEL = 2017;
    public static final int NIOE_PATH_IS_DIRECTORY = 2018;

    // endregion

    // region InvalidOperationException Codes

    static final int IOE_IN_MEMORY_READONLY_DB = 3001;
    static final int IOE_SORT_ON_ARRAY_TYPE = 3002;
    static final int IOE_REMOVE_DOCUMENT_ITERATOR = 3003;
    static final int IOE_OBJ_REMOVE_OBJECT_ITERATOR = 3004;
    static final int IOE_OBJ_REMOVE_PROJECTED_OBJECT_ITERATOR = 3005;
    public static final int IOE_OBJ_COMPOUND_INDEX = 3006;
    public static final int IOE_COMPOUND_INDEX = 3007;
    static final int IOE_REPLICATOR_RUNNING = 3008;
    public static final int IOE_DOC_ID_AUTO_GENERATED = 3009;
    static final int IOE_OBJ_REMOVE_JOINED_OBJECT_ITERATOR = 3010;
    static final int IOE_OBJ_MULTI_UPDATE_WITH_JUST_ONCE = 3011;

    // endregion

    // region FilterException Codes

    static final int FE_AND_INVALID = 4001;
    public static final int FE_GEO_EQ_FILTER_FIELD_NOT_INDEXED = 4002;
    static final int FE_ELEM_MATCH_NESTED = 4003;
    static final int FE_ELEM_MATCH_FULL_TEXT = 4004;
    static final int FE_ELEM_MATCH_NO_ARRAY = 4005;
    public static final int FE_ELEM_MATCH_INVALID_FILTER = 4006;
    public static final int FE_ELEM_MATCH_GT_FILTER_INVALID_FIELD = 4007;
    public static final int FE_ELEM_MATCH_GT_FILTER_INVALID_ITEM = 4008;
    public static final int FE_ELEM_MATCH_GTE_FILTER_INVALID_FIELD = 4009;
    public static final int FE_ELEM_MATCH_GTE_FILTER_INVALID_ITEM = 4010;
    public static final int FE_ELEM_MATCH_LT_FILTER_INVALID_FIELD = 4011;
    public static final int FE_ELEM_MATCH_LT_FILTER_INVALID_ITEM = 4012;
    public static final int FE_ELEM_MATCH_LTE_FILTER_INVALID_FIELD = 4013;
    public static final int FE_ELEM_MATCH_LTE_FILTER_INVALID_ITEM = 4014;
    public static final int FE_ELEM_MATCH_INVALID_REGEX = 4015;
    public static final int FE_ELEM_MATCH_REGEX_INVALID_ITEM = 4016;
    public static final int FE_GTE_FIELD_NOT_COMPARABLE = 4017;
    public static final int FE_GT_FIELD_NOT_COMPARABLE = 4018;
    public static final int FE_LTE_FIELD_NOT_COMPARABLE = 4019;
    public static final int FE_LT_FIELD_NOT_COMPARABLE = 4020;
    static final int FE_OR_INVALID = 4021;
    public static final int FE_REGEX_NO_STRING_VALUE = 4022;
    static final int FE_FTS_STAR_NOT_VALID = 4023;
    static final int FE_FILTERED_FIND_FAILED = 4024;
    static final int FE_FILTERED_FIND_WITH_OPTION_FAILED = 4025;
    public static final int FE_EQ_NOT_SPATIAL = 4026;
    public static final int FE_INTERSECTS_FILTER_FIELD_NOT_INDEXED = 4027;
    public static final int FE_WITHIN_FILTER_FIELD_NOT_INDEXED = 4028;
    public static final int FE_POINT_NULL = 4029;
    public static final int FE_IN_SEARCH_TERM_NOT_COMPARABLE = 4030;
    public static final int FE_SEARCH_TERM_NOT_COMPARABLE = 4031;
    public static final int FE_TEXT_FILTER_FIELD_NOT_INDEXED = 4032;
    static final int FE_SEARCH_TERM_INVALID_LEADING_STAR = 4033;
    static final int FE_SEARCH_TERM_INVALID_TRAILING_STAR = 4034;
    static final int FE_MULTIPLE_WORDS_WITH_WILDCARD = 4035;
    public static final int FE_EQUAL_NOT_COMPARABLE = 4036;
    public static final int FE_NOT_IN_SEARCH_TERM_NOT_COMPARABLE = 4037;

    // endregion

    // region IndexingException Codes

    public static final int IE_REBUILD_INDEX_FIELD_NOT_INDEXED = 5001;
    public static final int IE_VALIDATE_REBUILD_INDEX_RUNNING = 5002;
    static final int IE_FULL_TEXT_NON_STRING_VALUE = 5003;
    public static final int IE_INDEX_EXISTS = 5004;
    public static final int IE_INVALID_TYPE_FOR_INDEX = 5005;
    public static final int IE_CAN_NOT_DROP_ALL_RUNNING_INDEX = 5006;
    public static final int IE_CAN_NOT_DROP_RUNNING_INDEX = 5007;
    public static final int IE_REBUILD_INDEX_RUNNING = 5008;
    public static final int IE_DROP_NON_EXISTING_INDEX = 5009;
    public static final int IE_REMOVE_FULL_TEXT_INDEX_FAILED = 5010;
    public static final int IE_REBUILD_INDEX_NON_SPATIAL = 5011;
    public static final int IE_INDEX_ON_ARRAY_NOT_SUPPORTED = 5012;
    public static final int IE_INDEX_ON_NON_COMPARABLE_FIELD = 5013;
    public static final int IE_OBJ_INDEX_ON_ARRAY_NOT_SUPPORTED = 5014;
    public static final int IE_OBJ_INDEX_ON_NON_COMPARABLE_FIELD = 5015;
    public static final int IE_FAILED_TO_WRITE_FTS_DATA = 5016;
    static final int IE_FAILED_TO_QUERY_FTS_DATA = 5017;

    // endregion

    // region InvalidIdException Codes

    static final int IIE_COMPARISON_WITH_NULL_ID = 6001;
    public static final int IIE_INVALID_ID_FOUND = 6002;
    static final int IIE_NULL_ID = 6003;
    static final int IIE_ID_FIELD_NOT_ACCESSIBLE = 6004;
    static final int IIE_ID_VALUE_EMPTY_STRING = 6005;
    static final int IIE_AUTO_ID_ALREADY_SET = 6006;
    static final int IIE_NULL_ID_FILTER_VALUE = 6007;
    static final int IIE_CANNOT_ACCESS_AUTO_ID = 6008;
    static final int IIE_NULL_VALUE_ID = 6009;

    // endregion

    // region SecurityException Codes

    static final int SE_INVALID_USER_PASSWORD = 7001;
    static final int SE_NO_USER_MAP_FOUND = 7002;
    static final int SE_USER_MAP_SHOULD_NOT_EXISTS = 7003;
    static final int SE_NULL_USER_CREDENTIAL = 7004;
    static final int SE_USER_ID_EMPTY = 7005;
    static final int SE_PASSWORD_EMPTY = 7006;
    public static final int SE_HASHING_FAILED = 7007;

    // endregion

    // region NotIdentifiableException Codes

    static final int NIE_REMOVE_FAILED_FOR_NO_ID = 8001;
    static final int NIE_OBJ_REMOVE_FAILED_FOR_NO_ID = 8002;
    static final int NIE_OBJ_UPDATE_FAILED_FOR_NO_ID = 8003;
    static final int NIE_OBJ_MULTIPLE_ID = 8004;
    static final int NIE_UPDATE_FAILED_FOR_NO_ID = 8005;

    // endregion

    // region ObjectMappingException Codes

    public static final int OME_CYCLE_DETECTED = 9001;
    public static final int OME_NO_DEFAULT_CTOR = 9002;
    public static final int OME_PARSE_JSON_FAILED = 9003;
    static final int OME_SERIALIZE_TO_JSON_FAILED = 9004;
    public static final int OME_INSTANTIATE_FAILED = 9005;

    // endregion

    // region UniqueConstraintException Codes

    public static final int UCE_CONSTRAINT_VIOLATED = 10001;
    public static final int UCE_BUILD_INDEX_CONSTRAINT_VIOLATED = 10002;
    public static final int UCE_WRITE_INDEX_CONSTRAINT_VIOLATED = 10003;
    public static final int UCE_UPDATE_INDEX_CONSTRAINT_VIOLATED = 10004;

    // endregion

    // region SyncException Codes

    public static final int SYE_GET_ACCOUNT_REMOTE_ERROR = 11001;
    static final int SYE_CREATE_ACCOUNT_REMOTE_ERROR = 11002;
    public static final int SYE_CREATE_ACCOUNT_FAILED = 11003;
    public static final int SYE_DELETE_ACCOUNT_FAILED = 11004;
    public static final int SYE_DELETE_ACCOUNT_REMOTE_ERROR = 11005;
    static final int SYE_GET_SIZE_REMOTE_ERROR = 11006;
    static final int SYE_CLEAR_REMOTE_ERROR = 11007;
    public static final int SYE_CLEAR_FAILED = 11008;
    static final int SYE_FETCH_REMOTE_ERROR = 11009;
    static final int SYE_CHANGE_SINCE_REMOTE_ERROR = 11010;
    static final int SYE_CHANGE_REMOTE_ERROR = 11011;
    static final int SYE_TRY_LOCK_REMOTE_ERROR = 11012;
    public static final int SYE_RELEASE_LOCK_FAILED = 11013;
    static final int SYE_RELEASE_LOCK_REMOTE_ERROR = 11014;
    public static final int SYE_GET_ACCOUNT_FAILED = 11015;
    public static final int SYE_CHANGE_SINCE_FAILED = 11016;
    public static final int SYE_CHANGE_FAILED = 11017;
    public static final int SYE_FETCH_FAILED = 11018;
    public static final int SYE_SIZE_FAILED = 11019;
    public static final int SYE_IS_ONLINE_FAILED = 11020;
    public static final int SYE_TRY_LOCK_FAILED = 11021;
    public static final int SYE_UPDATE_ACCOUNT_FAILED = 11022;
    static final int SYE_UPDATE_ACCOUNT_REMOTE_ERROR = 11023;

    // endregion
}

