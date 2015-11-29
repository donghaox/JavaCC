package wci.intermediate.symtabimpl;

import wci.intermediate.*;

import java.util.ArrayList;

import static wci.intermediate.symtabimpl.DefinitionImpl.FUNCTION;
import static wci.intermediate.symtabimpl.DefinitionImpl.PROCEDURE;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.CONSTANT_VALUE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeFormImpl.SCALAR;
import static wci.intermediate.typeimpl.TypeKeyImpl.ENUMERATION_CONSTANTS;

/**
 * <h1>Predefined</h1>
 *
 * <p>Enter the predefined Pascal types, identifiers, and constants
 * into the symbol table.</p>
 *
 * <p>Copyright (c) 2008 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class Predefined
{
    // Predefined types.
    public static TypeSpec integerType;
    public static TypeSpec realType;
    public static TypeSpec booleanType;
    public static TypeSpec charType;
    public static TypeSpec voidType;
    public static TypeSpec undefinedType;

    // TODO: Predefined identifiers.
    public static SymTabEntry integerId;
    public static SymTabEntry realId;
    public static SymTabEntry booleanId;
    public static SymTabEntry charId;
    public static SymTabEntry voidId;
    public static SymTabEntry falseId;
    public static SymTabEntry trueId;
    public static SymTabEntry printId;

    /**
     * Initialize a symbol table stack with predefined identifiers.
     * @param symTabStack the symbol table stack to initialize.
     */
    public static void initialize(SymTabStack symTabStack)
    {
        initializeTypes(symTabStack);
        initializeConstants(symTabStack);
        initializeStandardRoutines(symTabStack);
    }

    /**
     * Initialize the predefined types.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeTypes(SymTabStack symTabStack)
    {
        // Type integer.
        integerId = symTabStack.enterLocal("int");
        integerType = TypeFactory.createType(SCALAR);
        integerType.setIdentifier(integerId);
        integerId.setDefinition(DefinitionImpl.TYPE);
        integerId.setTypeSpec(integerType);

        // Type real.
        realId = symTabStack.enterLocal("float");
        realType = TypeFactory.createType(SCALAR);
        realType.setIdentifier(realId);
        realId.setDefinition(DefinitionImpl.TYPE);
        realId.setTypeSpec(realType);

        // Type boolean.
        booleanId = symTabStack.enterLocal("bool");
        booleanType = TypeFactory.createType(ENUMERATION);
        booleanType.setIdentifier(booleanId);
        booleanId.setDefinition(DefinitionImpl.TYPE);
        booleanId.setTypeSpec(booleanType);

        // Type char.
        charId = symTabStack.enterLocal("string");
        charType = TypeFactory.createType(SCALAR);
        charType.setIdentifier(charId);
        charId.setDefinition(DefinitionImpl.TYPE);
        charId.setTypeSpec(charType);

        // Type void.
        voidId = symTabStack.enterLocal("void");
        voidType = TypeFactory.createType(SCALAR);
        voidType.setIdentifier(voidId);
        voidId.setDefinition(DefinitionImpl.TYPE);
        voidId.setTypeSpec(voidType);

        // Undefined type.
        undefinedType = TypeFactory.createType(SCALAR);
    }

    /**
     * Initialize the predefined constant.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeConstants(SymTabStack symTabStack)
    {
        // Boolean enumeration constant false.
        falseId = symTabStack.enterLocal("false");
        falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        falseId.setTypeSpec(booleanType);
        falseId.setAttribute(CONSTANT_VALUE, new Integer(0));

        // Boolean enumeration constant true.
        trueId = symTabStack.enterLocal("true");
        trueId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
        trueId.setTypeSpec(booleanType);
        trueId.setAttribute(CONSTANT_VALUE, new Integer(1));

        // Add false and true to the boolean enumeration type.
        ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();
        constants.add(falseId);
        constants.add(trueId);
        booleanType.setAttribute(ENUMERATION_CONSTANTS, constants);
    }

    /**
    TODO: Put the GO standard routines here
     * Initialize the standard procedures and functions.
     * @param symTabStack the symbol table stack to initialize.
     */
    private static void initializeStandardRoutines(SymTabStack symTabStack)
    {
        printId = enterStandard(symTabStack, FUNCTION, "print", PRINT);
    }

    /**
     * Enter a standard procedure or function into the symbol table stack.
     * @param symTabStack the symbol table stack to initialize.
     * @param defn either PROCEDURE or FUNCTION.
     * @param name the procedure or function name.
     */
    private static SymTabEntry enterStandard(SymTabStack symTabStack,
                                             Definition defn, String name,
                                             RoutineCode routineCode)
    {
        SymTabEntry procId = symTabStack.enterLocal(name);
        procId.setDefinition(defn);
        procId.setAttribute(ROUTINE_CODE, routineCode);

        return procId;
    }
}
