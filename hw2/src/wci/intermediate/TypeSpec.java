package wci.intermediate;

/**
 * Created by Haoxuan Dong on 10/9/2015.
 */
public interface TypeSpec {
    /**
     * getForm() returns the type of a Type specification
     * @return the form of Type such as SCALAR, ARRAY or RECORD
     */
     TypeForm getForm();
     void setIdentifier(SymTabEntry identifier);

    /**
     * get identifier
     * @return the symbol table entry of the ype identifier or null if the type
     * is unnamed
     */
     SymTabEntry getIdentifier();

    /**
     * All other type specification information is stored and retrieved as named
     * attribute values using methods setAttributes() and getAttributes()
     * @param key
     * @param value
     */
     void setAttribute(TypeKey key, Object value);
     Object getAttribute(TypeKey key);

    /**
     * isPascalString() returns whether or not the type represents a Pascal
     * string type
     * @return
     */
    boolean isPascalString();

    /**
     * baseType() returns the base type of a subrange type; for other types,
     * it simply returns the type itself.
     * @return
     */
    TypeSpec baseType();

}
