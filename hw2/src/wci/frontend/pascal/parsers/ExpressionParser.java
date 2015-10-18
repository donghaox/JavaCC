package wci.frontend.pascal.parsers;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.HashMap;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.symtabimpl.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;
import wci.intermediate.typeimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.symtabimpl.DefinitionImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SET;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.*;
/**
 * <h1>ExpressionParser</h1>
 *
 * <p>Parse a Pascal expression.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class ExpressionParser extends StatementParser
{
    /**
     * Constructor.
     * @param parent the parent parser.
     */
    public ExpressionParser(PascalParserTD parent)
    {
        super(parent);
    }

    // Synchronization set for starting an expression.
    static final EnumSet<PascalTokenType> EXPR_START_SET =
            EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING,
                    PascalTokenType.NOT, LEFT_PAREN, PascalTokenType.SET,LEFT_BRACKET);

    /**
     * Parse an expression.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ICodeNode parse(Token token)
            throws Exception
    {
        return parseExpression(token);
    }

    // Set of relational operators.
    private static final EnumSet<PascalTokenType> REL_OPS =
            EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
                    GREATER_THAN, GREATER_EQUALS, PascalTokenType.IN);

    // Map relational operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeType>
            REL_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
    static {
        REL_OPS_MAP.put(EQUALS, EQ);
        REL_OPS_MAP.put(NOT_EQUALS, NE);
        REL_OPS_MAP.put(LESS_THAN, LT);
        REL_OPS_MAP.put(LESS_EQUALS, LE);
        REL_OPS_MAP.put(GREATER_THAN, GT);
        REL_OPS_MAP.put(GREATER_EQUALS, GE);
        REL_OPS_MAP.put(IN, IN_CODE);
    };

    /**
     * Parse an expression.
     * @param token the initial token.
     * @return the root of the generated parse subtree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseExpression(Token token)
            throws Exception
    {
        // Parse a simple expression and make the root of its tree
        // the root node.
        ICodeNode rootNode = parseSimpleExpression(token);
        TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        token = currentToken();
        TokenType tokenType = token.getType();

        // Look for a relational operator.
        if (REL_OPS.contains(tokenType)) {

            // Create a new operator node and adopt the current tree
            // as its first child.
            ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();  // consume the operator

            // Parse the second simple expression.  The operator node adopts
            // the simple expression's tree as its second child.
            ICodeNode simExprNode = parseSimpleExpression(token);
            opNode.addChild(simExprNode);

            // The operator node becomes the new root node.
            rootNode = opNode;

            // Type check: The operands must be comparison compatible.
            TypeSpec simExprType = simExprNode != null
                    ? simExprNode.getTypeSpec()
                    : Predefined.undefinedType;
            if (TypeChecker.areComparisonCompatible(resultType, simExprType)) {
                resultType = Predefined.booleanType;
            }

            else if(resultType.getForm() == SCALAR && simExprType.getForm() == TypeFormImpl.SET){
                resultType = Predefined.booleanType;
            }

            else {
                errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                resultType = Predefined.undefinedType;
            }
        }

        if (rootNode != null) {
            rootNode.setTypeSpec(resultType);
        }

        return rootNode;
    }

    // Set of additive operators.
    private static final EnumSet<PascalTokenType> ADD_OPS =
            EnumSet.of(PLUS, MINUS, PascalTokenType.OR);

    // Map additive operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeTypeImpl>
            ADD_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeTypeImpl>();
    static {
        ADD_OPS_OPS_MAP.put(PLUS, ADD);
        ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
        ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
    };

    /**
     * Parse a simple expression.
     * @param token the initial token.
     * @return the root of the generated parse subtree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseSimpleExpression(Token token)
            throws Exception
    {
        TokenType signType = null;  // type of leading sign (if any)

        // Look for a leading + or - sign.
        TokenType tokenType = token.getType();
        if ((tokenType == PLUS) || (tokenType == MINUS)) {
            signType = tokenType;
            token = nextToken();  // consume the + or -
        }

        // Parse a term and make the root of its tree the root node.
        ICodeNode rootNode = parseTerm(token);
        TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                :Predefined.undefinedType;

        // Was there a leading - sign?
        if (signType == MINUS) {

            // Create a NEGATE node and adopt the current tree
            // as its child. The NEGATE node becomes the new root node.
            ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
            negateNode.addChild(rootNode);
            negateNode.setTypeSpec(rootNode.getTypeSpec());
            rootNode = negateNode;
        }

        token = currentToken();
        tokenType = token.getType();

        // Loop over additive operators.
        while (ADD_OPS.contains(tokenType)) {
            TokenType operator = tokenType;
            // Create a new operator node and adopt the current tree
            // as its first child.
            ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(operator);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();  // consume the operator

            // Parse another term.  The operator node adopts
            // the term's tree as its second child.
            ICodeNode termNode = parseTerm(token);
            opNode.addChild(termNode);
            TypeSpec termType = termNode != null ? termNode.getTypeSpec()
                    :Predefined.undefinedType;

            // The operator node becomes the new root node.
            rootNode = opNode;
            // Determine the result type.
            switch ((PascalTokenType) operator) {

                case PLUS:
                case MINUS: {
                    // Both operands integer ==> integer result.
                    if (TypeChecker.areBothInteger(resultType, termType)) {
                        resultType = Predefined.integerType;
                    }

                    // Both real operands or one real and one integer operand
                    // ==> real result.
                    else if (TypeChecker.isAtLeastOneReal(resultType,
                            termType)) {
                        resultType = Predefined.realType;
                    }

                    else if(TypeChecker.areBothSet(resultType, termType)){
                        resultType = TypeFactory.createType(TypeFormImpl.SET);
                    }

                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }

                case OR: {
                    // Both operands boolean ==> boolean result.
                    if (TypeChecker.areBothBoolean(resultType, termType)) {
                        resultType = Predefined.booleanType;
                    }
                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }
            }

            rootNode.setTypeSpec(resultType);

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    // Set of multiplicative operators.
    private static final EnumSet<PascalTokenType> MULT_OPS =
            EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND, DOT_DOT);

    // Map multiplicative operator tokens to node types.
    private static final HashMap<PascalTokenType, ICodeNodeType>
            MULT_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
    static {
        MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
        MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
        MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
        MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
        MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
        MULT_OPS_OPS_MAP.put(DOT_DOT, ICodeNodeTypeImpl.SET_RANGE);
    };

    /**
     * Parse a term.
     * @param token the initial token.
     * @return the root of the generated parse subtree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseTerm(Token token)
            throws Exception
    {
        // Parse a factor and make its node the root node.
        ICodeNode rootNode = parseFactor(token);

        TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                : Predefined.undefinedType;

        token = currentToken();
        TokenType tokenType = token.getType();

        if (rootNode.getType() == SET){
            if ((tokenType == SLASH) || tokenType == LESS_THAN || tokenType == PascalTokenType.OR){
                errorHandler.flag(token, INVALID_OPERATOR, this);
            }
        }

        if (rootNode.getType() == INTEGER_CONSTANT){
            if(tokenType == IN){
                //errorHandler.flag(token, INVALID_OPERATOR, this);
            }
        }

        // Loop over multiplicative operators.
        while (MULT_OPS.contains(tokenType)) {
            TokenType operator = tokenType;

            // Create a new operator node and adopt the current tree
            // as its first child.
            ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(tokenType);
            ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
            opNode.addChild(rootNode);

            token = nextToken();  // consume the operator

            // Parse another factor.  The operator node adopts
            // the term's tree as its second child.
            ICodeNode factorNode = parseFactor(token);
            opNode.addChild(factorNode);
            TypeSpec factorType = factorNode != null ? factorNode.getTypeSpec()
                    :Predefined.undefinedType;

            // The operator node becomes the new root node.
            rootNode = opNode;
            // Determine the result type.
            switch ((PascalTokenType) operator) {

                case STAR: {
                    // Both operands integer ==> integer result.
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    }

                    // Both real operands or one real and one integer operand
                    // ==> real result.
                    else if (TypeChecker.isAtLeastOneReal(resultType,
                            factorType)) {
                        resultType = Predefined.realType;
                    }
                    else if (TypeChecker.areBothSet(resultType, factorType)){
                        resultType = TypeFactory.createType(TypeFormImpl.SET);
                    }

                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }

                case SLASH: {
                    // All integer and real operand combinations
                    // ==> real result.
                    if (TypeChecker.areBothInteger(resultType, factorType) ||
                            TypeChecker.isAtLeastOneReal(resultType, factorType))
                    {
                        resultType = Predefined.realType;
                    }
                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }

                case DIV:
                case MOD: {
                    // Both operands integer ==> integer result.
                    if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    }
                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }

                case AND: {
                    // Both operands boolean ==> boolean result.
                    if (TypeChecker.areBothBoolean(resultType, factorType)) {
                        resultType = Predefined.booleanType;
                    }
                    else {
                        errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                    }

                    break;
                }
            }

            rootNode.setTypeSpec(resultType);

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    /**
     * Parse a factor.
     * @param token the initial token.
     * @return the root of the generated parse subtree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseFactor(Token token)
            throws Exception
    {
        TokenType tokenType = token.getType();
        ICodeNode rootNode = null;

        switch ((PascalTokenType) tokenType) {

            case IDENTIFIER: {
                return parseIdentifier(token);
            }

            case INTEGER: {
                // Create an INTEGER_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(VALUE, token.getValue());

                token = nextToken();  // consume the number
                rootNode.setTypeSpec(Predefined.integerType);
                break;
            }

            case REAL: {
                // Create an REAL_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                rootNode.setAttribute(VALUE, token.getValue());

                token = nextToken();  // consume the number
                rootNode.setTypeSpec(Predefined.realType);
                break;
            }

            case STRING: {
                String value = (String) token.getValue();

                // Create a STRING_CONSTANT node as the root node.
                rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                rootNode.setAttribute(VALUE, value);

                TypeSpec resultType = value.length() == 1
                        ? Predefined.charType : TypeFactory.createStringType(value);

                token = nextToken();  // consume the string

                rootNode.setTypeSpec(resultType);
                break;
            }

		/*            case SET: {
		 *//*  strToHashSet(token);
                HashSet<Integer> value = (HashSet<Integer>)token.getValue();*//*
                String value =(String)token.getValue();
                rootNode = ICodeFactory.createICodeNode(SET);
                rootNode.setAttribute(VALUE, value);
                token = nextToken();
                break;
            }*/
            case LEFT_BRACKET:{
                token = nextToken();// consume the [
                //create a SET node as the root node
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SET);

                //parse an expression and add it as a child
			/*                rootNode.addChild(parseExpression(token));

                //look for the matching .. , ] token
                token = currentToken();
                if (token.getType() == DOT_DOT){
                    token = nextToken();
                    rootNode.addChild(parseExpression(token));
                    token = currentToken();

                }else if (token.getType() == COMMA){
                    token = nextToken();
                    rootNode.addChild(parseExpression(token));
                    token = currentToken();

                }else if (token.getType() == LEFT_BRACKET){
                    token = nextToken();//consume the ]

                }else {
                    errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
                }*/

                parseSet(token, rootNode, MISSING_RIGHT_BRACKET);

                break;

            }

            case NOT: {
                token = nextToken();  // consume the NOT

                // Create a NOT node as the root node.
                rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

                // Parse the factor.  The NOT node adopts the
                // factor node as its child.
                ICodeNode factorNode = parseFactor(token);
                rootNode.addChild(factorNode);

                // Type check: The factor must be boolean.
                TypeSpec factorType = factorNode != null
                        ? factorNode.getTypeSpec()
                        : Predefined.undefinedType;
                if (!TypeChecker.isBoolean(factorType)) {
                    errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                }

                rootNode.setTypeSpec(Predefined.booleanType);
                break;
            }

            case LEFT_PAREN: {
            /*    token = nextToken();      // consume the (

                // Parse an expression and make its node the root node.
                rootNode = parseExpression(token);

                // Look for the matching ) token.
                token = currentToken();
                if (token.getType() == RIGHT_PAREN) {
                    token = nextToken();  // consume the )
                }
                else {
                    errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
                }*/
                token = nextToken();      // consume the (

                // Parse an expression and make its node the root node.
                rootNode = parseExpression(token);
                TypeSpec resultType = rootNode != null
                        ? rootNode.getTypeSpec()
                        : Predefined.undefinedType;

                // Look for the matching ) token.
                token = currentToken();
                if (token.getType() == RIGHT_PAREN) {
                    token = nextToken();  // consume the )
                }
                else {
                    errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
                }

                rootNode.setTypeSpec(resultType);

                break;
            }

            default: {
                errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                break;
            }
        }


        return rootNode;
    }

    protected void parseSet(Token token, ICodeNode parentNode,
                            PascalErrorCode errorCode)
            throws Exception
    {
        TypeSpec typeSpec = TypeFactory.createType(TypeFormImpl.SET);
        ICodeNode statementNode = null;

        // Synchronization set for the terminator.
        TokenType previous_token = token.getType();
        // Loop to parse each statement until the END token
        // or the end of the source file.
        while (!(token instanceof EofToken) &&
                (token.getType() != RIGHT_BRACKET)) {

            previous_token = token.getType();
            // Parse a statement.  The parent node adopts the statement node.
            if(token.getType() != COMMA){
                 statementNode = parse(token);

                //handle non unique member
                boolean is_unique = check_unique(token, parentNode, statementNode);
                boolean valid_set = valid_set(statementNode);
                if(valid_set){
                    if(is_unique){
                        parentNode.addChild(statementNode);
                    }
                    else{
                        errorHandler.flag(token, NONE_UNIQUE_MEMBER, this);
                    }
                }
            }

            token = currentToken();
            TokenType tokenType = token.getType();

            //handle missing comma
            if(previous_token == INTEGER && tokenType == INTEGER){
                errorHandler.flag(token, MISSING_COMMA, this);
            }

            // Look for the semicolon between statements.
            if (tokenType == COMMA) {
                token = nextToken();  // consume the ;
                //handle double comma
                if(tokenType == token.getType()){
                    errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                }
            }

            else if (tokenType == DOT_DOT){
                token = nextToken();
                if(token.getType() != INTEGER){
                    errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                }
            }

            else if (tokenType == SEMICOLON){
                errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
                return;
            }

            // Synchronize at the start of the next statement
            // or at the terminator.
        }

        // Look for the terminator token.
        if (token.getType() == RIGHT_BRACKET) {
            token = nextToken();  // consume the terminator token
        }
        else {
            errorHandler.flag(token, errorCode, this);
        }
        if(statementNode != null)
        typeSpec.setAttribute(SET_ELEMENT_TYPE, statementNode);

        parentNode.setTypeSpec(typeSpec);
    }

    protected boolean check_unique(Token token, ICodeNode pNode, ICodeNode child){
		/*
		 * this method make sure set's members are unique
		 */

        if(pNode.getChildren().contains(child)){
            if(child.getType() == MULTIPLY){
                return true;
            }
            return false;
        }

        for (int i = 0; i < pNode.getChildren().size(); i++){
            if (pNode.getChildren().get(i).getType() == SET_RANGE){
                int child_value = (int)child.getAttribute(VALUE);
                if(child_value >= (int)pNode.getChildren().get(i).getChildren().get(0).getAttribute(VALUE)
                        && child_value <= (int)pNode.getChildren().get(i).getChildren().get(1).getAttribute(VALUE)){
                    return false;
                }
            }
            else {
                if (child.getType() == SET_RANGE){
                    try{
                        int _value = (int)pNode.getChildren().get(i).getAttribute(VALUE);
                        if(_value >= (int)child.getChildren().get(0).getAttribute(VALUE)
                                && _value <= (int)child.getChildren().get(1).getAttribute(VALUE)){
                            return false;
                        }
                    }
                    catch (Exception e){
                        return true;
                    }
                }
            }
        }
        return true;
    }

    protected boolean valid_set(ICodeNode child){
        if (child.getType() != SET_RANGE){
            return true;
        }
        else{
            if (child.getChildren().size() < 2){
                return false;
            }
        }
        return true;

    }
    /**
     * Parse an identifier.
     * @param token the current token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    private ICodeNode parseIdentifier(Token token)
            throws Exception
    {
        ICodeNode rootNode = null;

        // Look up the identifier in the symbol table stack.
        String name = token.getText().toLowerCase();
        SymTabEntry id = symTabStack.lookup(name);

        // Undefined.
        if (id == null) {
            errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
            id = symTabStack.enterLocal(name);
            id.setDefinition(UNDEFINED);
            id.setTypeSpec(Predefined.undefinedType);
        }

        Definition defnCode = id.getDefinition();

        switch ((DefinitionImpl) defnCode) {

            case CONSTANT: {
                Object value = id.getAttribute(CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                if (value instanceof Integer) {
                    rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }
                else if (value instanceof Float) {
                    rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }
                else if (value instanceof String) {
                    rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                    rootNode.setAttribute(VALUE, value);
                }

                id.appendLineNumber(token.getLineNumber());
                token = nextToken();  // consume the constant identifier

                if (rootNode != null) {
                    rootNode.setTypeSpec(type);
                }

                break;
            }

            case ENUMERATION_CONSTANT: {
                Object value = id.getAttribute(CONSTANT_VALUE);
                TypeSpec type = id.getTypeSpec();

                rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                rootNode.setAttribute(VALUE, value);

                id.appendLineNumber(token.getLineNumber());
                token = nextToken();  // consume the enum constant identifier

                rootNode.setTypeSpec(type);
                break;
            }

            default: {
                VariableParser variableParser = new VariableParser(this);
                rootNode = variableParser.parse(token, id);
                break;
            }
        }

        return rootNode;
    }
}
