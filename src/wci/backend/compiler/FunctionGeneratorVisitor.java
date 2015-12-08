package wci.backend.compiler;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;

public class FunctionGeneratorVisitor extends ProlangParserVisitorAdapter
{
	static final int stack_limit = 17;
	
    public Object visit(ASTfunction_declaration node, Object data) {
        StringBuilder param_dec = new StringBuilder();
        StringBuilder var_dec = new StringBuilder(); 
        SymTabEntry function_name = (SymTabEntry) node.getAttribute(ID);
        SymTabImpl scope = (SymTabImpl) function_name.getAttribute(SymTabKeyImpl.ROUTINE_SYMTAB);
        
        //loop through function to generate jasmin code
        for (SymTabEntry parameter : scope.values()) {
            Definition param_def = parameter.getDefinition();
            if (param_def != DefinitionImpl.VARIABLE && param_def != DefinitionImpl.FUNCTION) {
                TypeSpec type = parameter.getTypeSpec();
                var_dec.append("    .var " + parameter.getIndex() + " is " + parameter.getName() + " ");
                
                //handle function with reference 
                if (param_def == DefinitionImpl.REFERENCE_PARAMETER) {
                    if (type == Predefined.integerType) {
                        param_dec.append("LIWrap;");
                        var_dec.append("LIWrap;\n");
                    }
                    else if (type == Predefined.realType) {
                        param_dec.append("LRWrap;");
                        var_dec.append("LRWrap;\n");
                    }
                    else if (type == Predefined.charType) {
                        param_dec.append("Ljava/lang/String;");
                        var_dec.append("Ljava/lang/String;\n");
                    }
                    else if (type == Predefined.booleanType) {
                        param_dec.append("LBWrap;");
                        var_dec.append("LBWrap;\n");
                    }
                }
                
                //handle function with copy
                else if (type == Predefined.integerType) {
                    param_dec.append("I");
                    var_dec.append("I\n");
                }
                else if (type == Predefined.realType) {
                    param_dec.append("F");
                    var_dec.append("F\n");
                }
                else if (type == Predefined.charType) {
                    param_dec.append("Ljava/lang/String;");
                    var_dec.append("Ljava/lang/String;\n");
                }
                else if (type == Predefined.booleanType) {
                    param_dec.append("Z");
                    var_dec.append("Z\n");
                }
            }
        }

        String func_return_type = (String) node.jjtGetChild(2).jjtAccept(this, data); 
        int local_count = 0;

        for (SymTabEntry entry : scope.values()) {
            if (entry.getDefinition() != DefinitionImpl.FUNCTION) {
                local_count++;
            }
        }

        //build function header
        CodeGenerator.objectFile.println(".method private static "
                + function_name.getName() + "(" + param_dec.toString() + ")" + func_return_type + "\n");
        CodeGenerator.objectFile.flush();
        CodeGenerator.objectFile.println(var_dec.toString());

        ProlangParserVisitor codeGenerator = new CodeGeneratorVisitor();
        
        //handle parameter
        node.jjtGetChild(1).jjtAccept(codeGenerator, data); 
        
        //handle function body
        node.jjtGetChild(3).jjtAccept(codeGenerator, data); 

        CodeGenerator.objectFile.println();
        
        //handle return, current support integer, boolean, string and float
        // integer and boolean are handled the same way, boolean value is 
        // handled 0 or 1
        if(func_return_type == "I" || func_return_type == "Z")
        {
        	CodeGenerator.objectFile.println("    iload_0");
        	CodeGenerator.objectFile.println("    ireturn");
        }
        //handle string return
        else if (func_return_type == "Ljava/lang/String;")
        {
        	CodeGenerator.objectFile.println("    aload_0");
        	CodeGenerator.objectFile.println("    areturn");
        }
        //handle float return
        else if(func_return_type == "F")
        {
        	CodeGenerator.objectFile.println("    fload_0");
        	CodeGenerator.objectFile.println("    freturn");
        }
      
        //finishing up function
        CodeGenerator.objectFile.println("    return");
	    CodeGenerator.objectFile.println();
	    CodeGenerator.objectFile.println(".limit locals " + local_count);
	    CodeGenerator.objectFile.println(".limit stack  " + stack_limit);
	    CodeGenerator.objectFile.println(".end method\n");
	    CodeGenerator.objectFile.flush();
      
        node.jjtGetChild(3).jjtAccept(this, data); 
        return data;
    }

    public Object visit(ASTreturnType node, Object data) {
        return CodeGeneratorVisitor.get_typedes(node);
    }
}