package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;
import static wci.frontend.Source.EOF;
import static wci.frontend.pascal.PascalErrorCode.*;


import java.util.ArrayList;
import java.util.HashSet;

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
     * @throws Exception if an error occurred.
     */
    public PascalSetToken(Source source)
            throws Exception
    {
        super(source);
    }
    //declaration
    protected HashSet<Integer> valueSet = new HashSet<>();

    protected void extract()
        throws Exception
    {
    	//todo
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append('[');
        char cur = nextChar();

        while(cur != ']' && cur != EOF){
            if (Character.isWhitespace(cur)) cur = ' ';
            textBuilder.append(cur);
        }
        String[] parts = textBuilder.toString().split(",");
        for (String e: parts) e.trim();//get rid of white spaces.

        strToHashSet(parts);
        if (cur == ']'){
            type = SET;
            value = valueSet;
            textBuilder.append(']');
            nextChar();
        }else {
            type = ERROR;
            value = UNEXPECTED_EOF;
        }

        text = textBuilder.toString();
    }

    private void strToHashSet(String[] parts){
        String[] temp;
        int n1, n2;
        for ( String e : parts){
            if (e.matches("\\d+")) valueSet.add(Integer.parseInt(e));
            else if (e.contains("..")) {
                 temp = e.split("..");
                n1 = Integer.parseInt(temp[0]);
                n2 = Integer.parseInt(temp[1]);
                while(n1 <= n2){
                    valueSet.add(n1);
                    n1++;
                }
            }
        }
    }
}
