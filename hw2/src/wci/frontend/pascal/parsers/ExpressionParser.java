package wci.frontend.pascal.parsers;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.frontend.pascal.tokens.PascalSetToken;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalTokenType.NOT;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SET;
import static  wci.intermediate.symtabimpl.SymTabKeyImpl.DATA_VALUE;

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
			opNode.addChild(parseSimpleExpression(token));

			// The operator node becomes the new root node.
			rootNode = opNode;
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

		// Was there a leading - sign?
		if (signType == MINUS) {

			// Create a NEGATE node and adopt the current tree
			// as its child. The NEGATE node becomes the new root node.
			ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
			negateNode.addChild(rootNode);
			rootNode = negateNode;
		}

		token = currentToken();
		tokenType = token.getType();

		// Loop over additive operators.
		while (ADD_OPS.contains(tokenType)) {

			// Create a new operator node and adopt the current tree
			// as its first child.
			ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(tokenType);
			ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
			opNode.addChild(rootNode);

			token = nextToken();  // consume the operator

			// Parse another term.  The operator node adopts
			// the term's tree as its second child.
			opNode.addChild(parseTerm(token));

			// The operator node becomes the new root node.
			rootNode = opNode;

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

		token = currentToken();
		TokenType tokenType = token.getType();
		
		if (rootNode.getType() == SET){
			if ((tokenType == SLASH) || tokenType == LESS_THAN || tokenType ==  IN || tokenType == PascalTokenType.OR){
				errorHandler.flag(token, INVALID_OPERATOR, this);
			}
		}
		
		if (rootNode.getType() == INTEGER_CONSTANT){
			if(tokenType == IN){
				errorHandler.flag(token, INVALID_OPERATOR, this);
			}
		}

		// Loop over multiplicative operators.
		while (MULT_OPS.contains(tokenType)) {

			// Create a new operator node and adopt the current tree
			// as its first child.
			ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(tokenType);
			ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
			opNode.addChild(rootNode);

			token = nextToken();  // consume the operator

			// Parse another factor.  The operator node adopts
			// the term's tree as its second child.
			opNode.addChild(parseFactor(token));

			// The operator node becomes the new root node.
			rootNode = opNode;

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
			// Look up the identifier in the symbol table stack.
			// Flag the identifier as undefined if it's not found.
			String name = token.getText().toLowerCase();
			SymTabEntry id = symTabStack.lookup(name);
			if (id == null) {
				errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
				id = symTabStack.enterLocal(name);
			}

			rootNode = ICodeFactory.createICodeNode(VARIABLE);
			rootNode.setAttribute(ID, id);
			id.appendLineNumber(token.getLineNumber());

			token = nextToken();  // consume the identifier
			break;
		}

		case INTEGER: {
			// Create an INTEGER_CONSTANT node as the root node.
			rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
			rootNode.setAttribute(VALUE, token.getValue());

			token = nextToken();  // consume the number
			break;
		}

		case REAL: {
			// Create an REAL_CONSTANT node as the root node.
			rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
			rootNode.setAttribute(VALUE, token.getValue());

			token = nextToken();  // consume the number
			break;
		}

		case STRING: {
			String value = (String) token.getValue();

			// Create a STRING_CONSTANT node as the root node.
			rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
			rootNode.setAttribute(VALUE, value);

			token = nextToken();  // consume the string
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
			rootNode.addChild(parseFactor(token));

			break;
		}

		case LEFT_PAREN: {
			token = nextToken();      // consume the (

			// Parse an expression and make its node the root node.
			rootNode = parseExpression(token);

			// Look for the matching ) token.
			token = currentToken();
			if (token.getType() == RIGHT_PAREN) {
				token = nextToken();  // consume the )
			}
			else {
				errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
			}

			break;
		}

		default: {
			errorHandler.flag(token, UNEXPECTED_TOKEN, this);
			break;
		}
		}


		return rootNode;
	}
	/*    private void strToHashSet(Token token){
        SymTabEntry entry;
        String str = (String)token.getValue();
        String[] parts = str.split(",");
        String[] temp;
        int n1;
        int n2;
        Object i;
        Set<Integer> valueSet = new HashSet<>();
        for ( String e : parts){
            if (e.matches("\\d+")) {
                valueSet.add(Integer.parseInt(e));
            }else if (e.matches("\\w+")){
                entry = symTabStack.getLocalSymTab().lookup(e);
                i = entry.getAttribute(DATA_VALUE);
                System.out.println("dd "+ entry.getName() + entry.get(DATA_VALUE));
            }
            else if (e.contains("..")) {
                 temp = e.split("\\.\\.");
                n1 = Integer.parseInt(temp[0]);
                n2 = Integer.parseInt(temp[1]);
                while(n1 <= n2){
                    valueSet.add(n1);
                    n1++;
                }
            }
        }
        ((PascalSetToken)token).setValue(valueSet);
    }*/
	protected void parseSet(Token token, ICodeNode parentNode,
			PascalErrorCode errorCode)
					throws Exception
	{
		// Synchronization set for the terminator.
		TokenType previous_token = token.getType();
		// Loop to parse each statement until the END token
		// or the end of the source file.
		while (!(token instanceof EofToken) &&
				(token.getType() != RIGHT_BRACKET)) {

			previous_token = token.getType();
			// Parse a statement.  The parent node adopts the statement node.
			if(token.getType() != COMMA){
				ICodeNode statementNode = parse(token);

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
	}

	protected boolean check_unique(Token token, ICodeNode pNode, ICodeNode child){
		/*
		 * this method make sure set's members are unique
		 */

		if(pNode.getChildren().contains(child)){
			return  false;
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
					int _value = (int)pNode.getChildren().get(i).getAttribute(VALUE);
					if(_value >= (int)child.getChildren().get(0).getAttribute(VALUE) 
							&& _value <= (int)child.getChildren().get(1).getAttribute(VALUE)){
						return false;
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
}
