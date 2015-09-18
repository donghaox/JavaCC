package wci.frontend.java.tokens;

import wci.frontend.*;
import wci.frontend.java.*;

import static wci.frontend.java.JavaTokenType.*;

/**
 * author hai
 */
public class JavaCharacterToken extends JavaToken
{
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public JavaCharacterToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a Java word token from the source.
     * @throws Exception if an error occurred.
     */
    protected void extract()
        throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();
        char currentChar = currentChar();
        	
        if(currentChar == '\''){
        	currentChar = nextChar();
        	if(Character.isLetter(currentChar)){
        		textBuffer.append(currentChar);
        		nextChar();
        		nextChar();
        	}
        	else if(currentChar == '\\'){
        		char tmp = currentChar;
        		currentChar = nextChar();
        		if(currentChar == 't' || currentChar == 'n'){
              	textBuffer.append(tmp);
        		textBuffer.append(currentChar);
        		nextChar();
        		nextChar();
        		}
        		else{
        			textBuffer.append(currentChar);
        			nextChar();
        			nextChar();
        		}
        	}
        	else{
        		//error must be character or one of the escape sequence
        	}
        }
        else{
        	//error must start with '
        }
       
        text = textBuffer.toString();

        // Is it a reserved word or an identifier?
        type = (RESERVED_WORDS.contains(text))
               ? JavaTokenType.valueOf(text)  // reserved word
               : CHARACTER;                    // identifier
    }
}
