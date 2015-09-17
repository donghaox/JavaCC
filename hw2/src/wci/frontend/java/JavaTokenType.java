package wci.frontend.java;

import java.util.Hashtable;
import java.util.HashSet;

import wci.frontend.TokenType;

/**
 * author hai
 */
public enum JavaTokenType implements TokenType
{
    // Reserved words.
	ABSTRACT, BREAK, CASE, CHAR, CLASS, CONST, CONTINUE, DO, DOUBLE,
    ELSE, ENUM, EXTENDS, FLOAT, FOR, GOTO, IF, INT, LONG, NATIVE,
    RETURN, SHORT, PACKAGE, PROTECTED, STATIC, SWITCH, SUPER,
    THIS, THROW, VOID, VOLATILE, WHILE,

    // Special symbols.
    TILT("~"), PIPE("|"), SINGLE_QUOTE("'"), DOUBLE_PLUS("++"), EQUAL_EQUAL("=="),
    DOUBLE_SLASH("//"), EXCLAIM("!"), SLASH("/"), DOUBLE_QUOTE("\""), MINUS_MINUS("--"),
    PIPE_EQUAL("|="), SLASH_STAR("/*"), AT("@"), COLON(":"), LEFT_PAREN("("),
    LESS_LESS("<<"), PERCENT_EQUAL("%="), STAR_SLASH("*/"), PERCENT("%"),SEMI_COLON(";"),
    RIGHT_PAREN(")"), MORE_MORE(">>"), AND_EQUAL("&="), UP_ARROW("^"), QUESTION_MARK("?"),
    LEFT_BRACKET("["), LESS_EQUAL("<="), UP_EQUAL("^="), AND_SYM("&"), LESS("<"), 
    RIGHT_BRACKET("]"), MORE_EQUAL(">="), EXCLAIM_EQUAL("!="), STAR("*"), MORE(">"), 
    LEFT_CURLY_BRACKET("{"), PLUS_EQUAL("+="), LESS_LESS_EQUAL("<<="), MINUS("-"), DOT("."),
    RIGHT_CURLY_BRACKET("}"), MINUS_EQUAL("-="), MORE_MORE_EQUAL(">>="), PLUS("+"), COMMA(","),
    STAR_EQUAL("*="), PIPE_PIPE("||"), EQUAL("="), SLASH_EQUAL("/="), AND_AND("&&"),

    IDENTIFIER, INTEGER, REAL, STRING,
    ERROR, END_OF_FILE;

    private static final int FIRST_RESERVED_INDEX = ABSTRACT.ordinal();
    private static final int LAST_RESERVED_INDEX  = WHILE.ordinal();

    private static final int FIRST_SPECIAL_INDEX = TILT.ordinal();
    private static final int LAST_SPECIAL_INDEX  = AND_AND.ordinal();

    private String text;  // token text

    /**
     * Constructor.
     */
    JavaTokenType()
    {
        this.text = this.toString();
    }

    /**
     * Constructor.
     * @param text the token text.
     */
    JavaTokenType(String text)
    {
        this.text = text;
    }

    /**
     * Getter.
     * @return the token text.
     */
    public String getText()
    {
        return text;
    }

    //java reserved word text strings.
    public static HashSet<String> RESERVED_WORDS = new HashSet<String>();
    static {
    	JavaTokenType values[] = JavaTokenType.values();
        for (int i = FIRST_RESERVED_INDEX; i <= LAST_RESERVED_INDEX; ++i) {
            RESERVED_WORDS.add(values[i].getText().toLowerCase());
        }
    }

    // Hash table of Java special symbols.  Each special symbol's text
    // is the key to its Java token type.
    public static Hashtable<String, JavaTokenType> SPECIAL_SYMBOLS =
        new Hashtable<String, JavaTokenType>();
    static {
    	JavaTokenType values[] = JavaTokenType.values();
        for (int i = FIRST_SPECIAL_INDEX; i <= LAST_SPECIAL_INDEX; ++i) {
            SPECIAL_SYMBOLS.put(values[i].getText(), values[i]);
        }
    }
}
