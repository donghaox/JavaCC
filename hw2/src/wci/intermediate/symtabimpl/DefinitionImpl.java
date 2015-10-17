package wci.intermediate.symtabimpl;

import wci.intermediate.Definition;

/**
 * Created by Haoxuan Dong on 10/9/2015.
 */
public enum DefinitionImpl implements Definition
{
    CONSTANT, ENUMERATION_CONSTANT("eunumeration constant"),
    TYPE, VARIABLE, FIELD("record field"),
    VALUE_PARM("value parameter"), VAR_PARM("variable parameter"),
    PROGRAM_PRAM("program parameter"),
    PROGRAM, PROCEDURE, FUNCTION,
    UNDEFINED;

    private String text;
    DefinitionImpl()
    {
        this.text = this.toString().toLowerCase();
    }

    DefinitionImpl(String text)
    {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
}
