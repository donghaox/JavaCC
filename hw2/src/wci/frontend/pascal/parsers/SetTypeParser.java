package wci.frontend.pascal.parsers;

import java.util.EnumSet;
import java.util.ArrayList;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.SET;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;


class SetTypeParser extends TypeSpecificationParser
{
    /**
     * Constructor.
     * @param parent the parent parser.
     */
    protected SetTypeParser(PascalParserTD parent)
    {
        super(parent);
    }


    // Synchronization set for OF.
    private static final EnumSet<PascalTokenType> OF_SET =
            TypeSpecificationParser.TYPE_START_SET.clone();
    static {
        OF_SET.add(OF);
        OF_SET.add(SEMICOLON);
    }

    private static final EnumSet<PascalTokenType> ELEMENT_START_SET =
            EnumSet.of(IDENTIFIER, INTEGER, REAL, LEFT_PAREN);

    /**
     * Parse a Pascal array type specification.
     * @param token the current token.
     * @return the array type specification.
     * @throws Exception if an error occurred.
     */
    public TypeSpec parse(Token token)
            throws Exception
    {
        TypeSpec setType = TypeFactory.createType(SET);
        token = nextToken();  // consume SET

        // Synchronize at OF.
        token = synchronize(OF_SET);
        if (token.getType() == OF) {
            token = nextToken();  // consume OF
        }
        else {
            errorHandler.flag(token, MISSING_OF, this);
        }
        token = synchronize(ELEMENT_START_SET);
        TokenType tokenType = token.getType();
        if (ELEMENT_START_SET.contains(tokenType)) {

            setType.setAttribute(SET_ELEMENT_TYPE, parseElementType(token));
        }
        else {
            errorHandler.flag(token, UNEXPECTED_TOKEN, this);
        }

        return setType;
    }
    /**
     * Parse the element type specification.
     * @param token the current token.
     * @return the element type specification.
     * @throws Exception if an error occurred.
     */
    private TypeSpec parseElementType(Token token)
            throws Exception
    {
        TypeSpecificationParser typeSpecificationParser =
                new TypeSpecificationParser(this);
        return typeSpecificationParser.parse(token);
    }
}
