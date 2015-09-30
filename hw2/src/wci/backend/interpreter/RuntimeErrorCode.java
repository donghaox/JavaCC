package wci.backend.interpreter;

/**
 * <h1>RuntimeErrorCode</h1>
 *
 * <p>Runtime error codes.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public enum RuntimeErrorCode
{
    UNINITIALIZED_VALUE("Uninitialized value"),
    VALUE_RANGE("Value out of range"),
    INVALID_CASE_EXPRESSION_VALUE("Invalid CASE expression value"),
    DIVISION_BY_ZERO("Division by zero"),
    INVALID_STANDARD_FUNCTION_ARGUMENT("Invalid standard function argument"),
    INVALID_INPUT("Invalid input"),
    STACK_OVERFLOW("Runtime stack overflow"),
    UNIMPLEMENTED_FEATURE("Unimplemented runtime feature"),

    //set operation errors
    MISSING_COMMA("MISSING_COMMA"),
    EXTRA_COMMA("EXTRA_COMMA"),
    MISSING_CLOSE_SQUARE_BRACKET("Missing close square bracket"),
    NON_UNIQUE_MEMBERS( "Non unique members"),
    INVALID_RANGE_OF_VALUES("Invalid range of values"),
    INVALID_OPERATOR("INVALID_OPERATOR");
    private String message;  // error message

    /**
     * Constructor.
     * @param message the error message.
     */
    RuntimeErrorCode(String message)
    {
        this.message = message;
    }

    public String toString()
    {
        return message;
    }
}
