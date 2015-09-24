package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;

/**
 * <h1>PascalSetToken</h1>
 * author: hai
 */
public class PascalSetToken extends PascalToken
{
    /**
     * Constructor.
     * @param source the source from where to fetch subsequent characters.
     * @param errorCode the error code.
     * @param tokenText the text of the erroneous token.
     * @throws Exception if an error occurred.
     */
    public PascalSetToken(Source source)
        throws Exception
    {
        super(source);
    }

    protected void extract()
        throws Exception
    {
    	//todo
    }
}
