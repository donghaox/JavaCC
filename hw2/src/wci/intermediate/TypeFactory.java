package wci.intermediate;
import wci.intermediate.typeimpl.*;

/**
 * Created by Haoxuan Dong on 10/9/2015.
 */
public class TypeFactory {

    public static TypeSpec createType(TypeForm form)
    {
        return  new TypeSpecImpl(form);
    }

    public static TypeSpec createStringType(String value)
    {
        return new TypeSpecImpl(value);
    }

}
