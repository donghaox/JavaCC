package wci.intermediate;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import wci.backend.compiler.CodeGenerator;
import wci.backend.compiler.CodeGeneratorVisitor;
import wci.frontend.*;
import wci.intermediate.symtabimpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;


public class ProlangParserVisitorAdapter implements ProlangParserVisitor {
    public Object visit(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTidentifier node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTintegerConstant node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTbooleanConstant node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTrealConstant node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTvoidConstant node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTinterpretedString node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTparameterList node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTparameter node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTreturnType node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTblock node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTcaseGroup node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTexpressionList node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTadd node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTsubtract node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTmultiply node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTdivide node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTmodulo node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTprintln node, Object data) {
        return node.childrenAccept(this, data);
    }
    
	public Object visit(ASTprint node, Object data) {
		return node.childrenAccept(this, data);
	}

    public Object visit(ASTequalEqual node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTlessThan node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTgreaterThan node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTnotEqual node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTlessEqual node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTgreaterEqual node, Object data) {
        return node.childrenAccept(this, data);
    }

	public Object visit(ASTif_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	public Object visit(ASTelse_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	public Object visit(ASTfor_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTfor_header node, Object data) {
		   return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTfunction_call node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTvariable_declaration node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTfunction_declaration node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTassignment_statement node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTstatement_list node, Object data) {
		return node.childrenAccept(this, data);
	} 
	
	public Object visit(ASTfunctionDeclaration node, Object data) {
        SymTabEntry functionId = (SymTabEntry) node.getAttribute(ID);
        SymTabImpl scope = (SymTabImpl) functionId.getAttribute(SymTabKeyImpl.ROUTINE_SYMTAB);
        StringBuilder typeBuffer = new StringBuilder(); // Used to store list of parameter types
        StringBuilder initBuffer = new StringBuilder(); // Used to store local variable initialization code

        for (SymTabEntry parameter : scope.values()) {
            Definition parameterDefinition = parameter.getDefinition();

            if (parameterDefinition != DefinitionImpl.VARIABLE && parameterDefinition != DefinitionImpl.FUNCTION) {
                TypeSpec type = parameter.getTypeSpec();
                initBuffer.append("    .var " + parameter.getIndex() + " is " + parameter.getName() + " ");

                if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                    if (type == Predefined.integerType) {
                        typeBuffer.append("LIWrap;");
                        initBuffer.append("LIWrap;\n");
                    }
                    else if (type == Predefined.realType) {
                        typeBuffer.append("LRWrap;");
                        initBuffer.append("LRWrap;\n");
                    }
                    else if (type == Predefined.charType) {
                        typeBuffer.append("Ljava/lang/String;");
                        initBuffer.append("Ljava/lang/String;\n");
                    }
                    else if (type == Predefined.booleanType) {
                        typeBuffer.append("LBWrap;");
                        initBuffer.append("LBWrap;\n");
                    }
                }
                else if (type == Predefined.integerType) {
                    typeBuffer.append("I");
                    initBuffer.append("I\n");
                }
                else if (type == Predefined.realType) {
                    typeBuffer.append("F");
                    initBuffer.append("F\n");
                }
                else if (type == Predefined.charType) {
                    typeBuffer.append("Ljava/lang/String;");
                    initBuffer.append("Ljava/lang/String;\n");
                }
                else if (type == Predefined.booleanType) {
                    typeBuffer.append("Z");
                    initBuffer.append("Z\n");
                }
            }
        }

        String returnType = (String) node.jjtGetChild(2).jjtAccept(this, data); // Get return type
        int localCount = 0;

        for (SymTabEntry entry : scope.values()) {
            if (entry.getDefinition() != DefinitionImpl.FUNCTION) {
                localCount++;
            }
        }

        CodeGenerator.objectFile.println(".method private static "
                + functionId.getName() + "(" + typeBuffer.toString() + ")" + returnType + "\n");
        CodeGenerator.objectFile.flush();
        CodeGenerator.objectFile.println(initBuffer.toString()); // Initialize local variables

        ProlangParserVisitor codeGenerator = new CodeGeneratorVisitor();
        node.jjtGetChild(1).jjtAccept(codeGenerator, data); // Process parameter list
        node.jjtGetChild(3).jjtAccept(codeGenerator, data); // Process function body

        CodeGenerator.objectFile.println();
        CodeGenerator.objectFile.println("    return");
        CodeGenerator.objectFile.println();
        CodeGenerator.objectFile.println(".limit locals " + localCount);
        CodeGenerator.objectFile.println(".limit stack  " + 16);
        CodeGenerator.objectFile.println(".end method\n");
        CodeGenerator.objectFile.flush();

        node.jjtGetChild(3).jjtAccept(this, data); // Process functions declared inside this function

        return data;
    }
	
}
