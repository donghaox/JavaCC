package wci.frontend.java.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.Source.EOL;
import static wci.frontend.Source.EOF;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

/**
 * author hai
 */
public class JavaStringToken extends PascalToken
{
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public JavaStringToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a java string token from the source.
     * @throws Exception if an error occurred.
     */
    protected void extract()
        throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        char currentChar = nextChar();  // consume initial quote
        textBuffer.append('\"');

        // Get string characters.
        do {
            // Replace any whitespace character with a blank.
            if (Character.isWhitespace(currentChar)) {
                currentChar = ' ';
            }

            if(currentChar == '\\'){
            	char nextChar = nextChar();
            	textBuffer.append(currentChar);
        		textBuffer.append(nextChar);
            	if(nextChar == 't'){
            		valueBuffer.append("\t");    		
            	}
            	else if(nextChar == 'n'){
            		valueBuffer.append("\n");
            	}
            	else{
            		valueBuffer.append(currentChar);
            		valueBuffer.append(nextChar);
            	}
            	currentChar = nextChar();
           
            }
            else if (currentChar != EOF && currentChar != '\"') {
                textBuffer.append(currentChar);
                valueBuffer.append(currentChar);
                currentChar = nextChar();  // consume character
            }
            
        } while ((currentChar != '\"') && (currentChar != EOF));

        if (currentChar == '\"') {
            nextChar();  // consume final quote
            textBuffer.append('\"');

            type = STRING;
            value = valueBuffer.toString();
        }
        else {
            type = ERROR;
            value = UNEXPECTED_EOF;
        }

        text = textBuffer.toString();
    }
}
