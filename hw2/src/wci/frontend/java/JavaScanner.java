package wci.frontend.java;

import wci.frontend.*;
import wci.frontend.java.tokens.JavaCharacterToken;
import wci.frontend.java.tokens.JavaErrorToken;
import wci.frontend.java.tokens.JavaNumberToken;
import wci.frontend.java.tokens.JavaSpecialSymbolToken;
import wci.frontend.java.tokens.JavaStringToken;
import wci.frontend.java.tokens.JavaWordToken;
import wci.frontend.pascal.tokens.*;
import static wci.frontend.Source.EOF;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

/*
 * author hai 
 */
public class JavaScanner extends Scanner
{
	/**
	 * Constructor
	 * @param source the source to be used with this scanner.
	 */
	public JavaScanner(Source source)
	{
		super(source);
	}

	/**
	 * Extract and return the next Java token from the source.
	 * @return the next token.
	 * @throws Exception if an error occurred.
	 */
	protected Token extractToken()
			throws Exception
			{
		skipWhiteSpace();

		Token token;
		char currentChar = currentChar();

		// Construct the next token.  The current character determines the
		// token type.
		if (currentChar == EOF) {
			token = new EofToken(source);
		}
		else if (Character.isLetter(currentChar)) {
			token = new JavaCharacterToken(source);
		}
		else if (Character.isDigit(currentChar)) {
			token = new JavaNumberToken(source);
		}
		else if (currentChar == '\"') {
			token = new JavaStringToken(source);
		}
		else if (currentChar == '\''){
			token = new JavaCharacterToken(source);
		}
		else if (JavaTokenType.SPECIAL_SYMBOLS
				.containsKey(Character.toString(currentChar))) {
			token = new JavaSpecialSymbolToken(source);
		}
		else {
			token = new JavaErrorToken(source, INVALID_CHARACTER,
					Character.toString(currentChar));
			nextChar();  // consume character
		}

		return token;
			}

	/**
	 * Skip whitespace characters by consuming them.  A comment is whitespace.
	 * @throws Exception if an error occurred.
	 */
	private void skipWhiteSpace()
			throws Exception
			{
		char currentChar = currentChar();
		boolean comment_ended = false;
		while ((Character.isWhitespace(currentChar) || (currentChar == '/'))) {

			if (currentChar == '/'){
				char nextChar = nextChar();
				// Start of a block comment
				if (nextChar == ('*')){
					while(true){
						currentChar = nextChar();
						if( currentChar == EOF){
							break;
						}
						if (currentChar == '*'){
							currentChar = nextChar();
							if(currentChar == '/'){
								currentChar = nextChar();
								comment_ended = true;
								break;
							}
						}
					}
				}
				// Start of line comment
				else if (nextChar == '/') {
					while(true){
						currentChar = nextChar();
						if (currentChar == '\n' || currentChar == EOF){
							currentChar = nextChar();
							comment_ended = true;
							break;
						}
					}
				}
			}

			// Not a comment.
			else {
				currentChar = nextChar();  // consume whitespace character
			}
		}
			}
}
