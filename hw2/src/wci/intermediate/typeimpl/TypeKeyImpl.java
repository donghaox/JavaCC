package wci.intermediate.typeimpl;

import wci.intermediate.TypeKey;

/**
 * Created by Haoxuan Dong on 10/9/2015.
 */
public enum  TypeKeyImpl implements TypeKey {

    //Enumeration
    ENUMERATION_CONSTANTS,

    //Subrange
    SUBRANGE_BASE_TYPE, SUBRANGE_MIN_VALUE,
    SUBRANGE_MAX_VALUE,

    //Array
    ARRAY_INDEX_TYPE, ARRAY_ELEMENT_TYPE,
    ARRAY_ELEMENT_COUNT,

    //RECORD
    RECORD_SYMTAB,

    //SET
    SET_ELEMENT_TYPE;
}
