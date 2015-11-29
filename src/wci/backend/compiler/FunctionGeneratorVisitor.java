package wci.backend.compiler;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;

public class FunctionGeneratorVisitor extends GoParserVisitorAdapter
{
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

        GoParserVisitor codeGenerator = new CodeGeneratorVisitor();
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

    public Object visit(ASTreturnType node, Object data) {
        TypeSpec type = node.getTypeSpec();
        String typeCode = null;

        if (type == Predefined.integerType) {
            typeCode ="I";
        }
        else if (type == Predefined.realType) {
            typeCode = "F";
        }
        else if (type == Predefined.charType) {
            typeCode = "Ljava/lang/String;";
        }
        else if (type == Predefined.booleanType) {
            typeCode = "Z";
        }
        else if (type == Predefined.voidType) {
            typeCode = "V";
        }

        return typeCode;
    }
}
