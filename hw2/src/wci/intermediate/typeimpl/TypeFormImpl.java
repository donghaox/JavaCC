package wci.intermediate.typeimpl;

import wci.intermediate.TypeForm;

/**
 * Created by Haoxuan Dong on 10/9/2015.
 */
public enum  TypeFormImpl implements TypeForm{
    SCALAR, ENUMERATION, SUBRANGE, ARRAY, RECORD, SET;

    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
