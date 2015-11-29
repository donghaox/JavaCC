package wci.backend.compiler;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeImpl;
import wci.intermediate.symtabimpl.*;

import java.util.ArrayList;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

public class CodeGeneratorVisitor extends GoParserVisitorAdapter implements GoParserTreeConstants
{
    private static String programName = null;
    private static int tagNumber = 0;

    public String getCurrentLabel() { return "label" + tagNumber; }
    public String getNextLabel() { return "label" + ++tagNumber; }

    public void emitComparisonCode(SimpleNode node, Object data) {
        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);
        SimpleNode rhs = (SimpleNode) node.jjtGetChild(1);

        TypeSpec lhsType = lhs.getTypeSpec();
        TypeSpec rhsType = rhs.getTypeSpec();

        lhs.jjtAccept(this, data);
        if (lhsType == Predefined.integerType) {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        rhs.jjtAccept(this, data);
        if (rhsType == Predefined.integerType) {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        CodeGenerator.objectFile.println("    fcmpg");
    }

    public Object visit(ASTstatementList node, Object data) {
        if (this.programName == null) {
            this.programName = (String) data;
        }

        return node.childrenAccept(this, data);
    }

    public Object visit(ASTassignmentStatement node, Object data)
    {
        String programName        = (String) data;
        SimpleNode variableNode   = (SimpleNode) node.jjtGetChild(0);
        SimpleNode expressionNode = (SimpleNode) node.jjtGetChild(1);
        SymTabEntry variableId = (SymTabEntry) variableNode.getAttribute(ID);
        Definition variableDefinition = variableId.getDefinition();
        String fieldName = variableId.getName();
        TypeSpec variableType = variableId.getTypeSpec();
        TypeSpec expressionType = expressionNode.getTypeSpec();
        TypeSpec targetType = node.getTypeSpec();
        String upperTypeCode = null;
        String lowerTypeCode = null;
        String wrapCode = null;

        // Emit code for the expression.
        if (variableDefinition != DefinitionImpl.REFERENCE_PARAMETER) {
            expressionNode.jjtAccept(this, data);
        }

        // Convert an integer value to float if necessary.
        if ((targetType == Predefined.realType) &&
                (expressionType == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        if (variableType == Predefined.integerType) {
            wrapCode = "I";
            upperTypeCode = "I";
            lowerTypeCode = "i";
        }
        else if (variableType == Predefined.realType) {
            wrapCode = "R";
            upperTypeCode = "F";
            lowerTypeCode = "f";
        }
        else if (variableType == Predefined.charType) {
            wrapCode = "Ljava/lang/String;";
            upperTypeCode = "Ljava/lang/String;";
            lowerTypeCode = "Ljava/lang/String;"; // TODO: How to load a local variable string?
        }
        else if (variableType == Predefined.booleanType) {
            wrapCode = "B";
            upperTypeCode = "Z";
            lowerTypeCode = "z";
        }

        // Emit the appropriate store instruction.
        if (variableDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
            CodeGenerator.objectFile.println("    aload " + variableId.getIndex());
            expressionNode.jjtAccept(this, data);
            CodeGenerator.objectFile.println("    putfield " + wrapCode + "Wrap/value " + upperTypeCode);
        }
        else if (variableId.getSymTab().getNestingLevel() == 1) {
            CodeGenerator.objectFile.println("    putstatic " + programName +
                    "/" + fieldName + " " + upperTypeCode);
        }
        else {
            CodeGenerator.objectFile.println("    " + lowerTypeCode + "store " + variableId.getIndex());
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTfunctionDeclaration node, Object data) {
        return data;
    }

    public Object visit(ASTfunctionCall node, Object data) {
        SymTabEntryImpl functionId = (SymTabEntryImpl) node.getAttribute(ID);
        SymTabImpl scope = (SymTabImpl) functionId.getAttribute(SymTabKeyImpl.ROUTINE_SYMTAB);
        TypeSpec returnType = node.getTypeSpec();
        ArrayList<SimpleNode> unwrapReferences = new ArrayList<SimpleNode>();
        String returnTypeCode = null;
        StringBuilder parameters = new StringBuilder();

        if (returnType == Predefined.integerType) {
            returnTypeCode = "I";
        }
        else if (returnType == Predefined.realType) {
            returnTypeCode = "F";
        }
        else if (returnType == Predefined.charType) {
            returnTypeCode = "Ljava/lang/String;"; // TODO: How to load a local variable string?
        }
        else if (returnType == Predefined.booleanType) {
            returnTypeCode = "Z";
        }
        else if (returnType == Predefined.voidType) {
            returnTypeCode = "V";
        }

        for (SymTabEntry parameter : scope.values()) {
            Definition parameterDefinition = parameter.getDefinition();

            if (parameterDefinition != DefinitionImpl.FUNCTION) {
                int index = parameter.getIndex();
                SimpleNode parameterNode = (SimpleNode) node.jjtGetChild(index + 1);
                TypeSpec parameterType = parameterNode.getTypeSpec();
                SymTabEntry parameterEntry = (SymTabEntry) parameterNode.getAttribute(ID);

                String upperTypeCode = null;
                String lowerTypeCode = null;
                String wrapTypeCode = null;

                if (parameterType == Predefined.integerType) {
                    upperTypeCode = "I";
                    lowerTypeCode = "i";

                    if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                        wrapTypeCode = "I";
                        parameters.append("LIWrap;");
                    }
                    else {
                        parameters.append("I");
                    }
                }
                else if (parameterType == Predefined.realType) {
                    upperTypeCode = "F";
                    lowerTypeCode = "f";

                    if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                        wrapTypeCode = "R";
                        parameters.append("LRWrap;");
                    }
                    else {
                        parameters.append("F");
                    }
                }
                else if (parameterType == Predefined.charType) {
                    wrapTypeCode = "Ljava/lang/String;";
                    parameters.append("Ljava/lang/String;"); // TODO: How to load a local variable string?
                    upperTypeCode = "Ljava/lang/String;";
                    lowerTypeCode = "Ljava/lang/String;";
                }
                else if (parameterType == Predefined.booleanType) {
                    upperTypeCode = "Z";
                    lowerTypeCode = "z";

                    if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                        wrapTypeCode = "B";
                        parameters.append("LBWrap;");
                    }
                    else {
                        parameters.append("Z");
                    }
                }

                if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                    CodeGenerator.objectFile.println("    new " + wrapTypeCode + "Wrap ");
                    CodeGenerator.objectFile.println("    dup");
                    CodeGenerator.objectFile.flush();
                }

                if (parameterEntry == null) {
                    parameterNode.jjtAccept(this, data);
                }
                else if (functionId.getSymTab().getNestingLevel() == 1) {
                    CodeGenerator.objectFile.println("    getstatic " + programName +
                            "/" + parameterEntry.getName() + " " + upperTypeCode);
                    CodeGenerator.objectFile.flush();
                }
                else {
                    int slot = parameterEntry.getIndex();
                    if (parameterEntry.getSymTab().getNestingLevel() == 1) {
                        slot++;
                    }
                    CodeGenerator.objectFile.println("    " + lowerTypeCode + "load " + slot);
                    CodeGenerator.objectFile.flush();
                }

                if (parameterDefinition == DefinitionImpl.REFERENCE_PARAMETER) {
                    CodeGenerator.objectFile.println("    invokenonvirtual " + wrapTypeCode
                            + "Wrap/<init>(" + upperTypeCode + ")" + returnTypeCode);
                    CodeGenerator.objectFile.println("    dup");
                    int slot = parameterEntry.getIndex();
                    if (parameterEntry.getSymTab().getNestingLevel() == 1) {
                        slot++;
                    }
                    CodeGenerator.objectFile.println("    astore " + slot);
                    CodeGenerator.objectFile.flush();
                    unwrapReferences.add(parameterNode);
                }
            }
        }

        CodeGenerator.objectFile.println("    invokestatic  " + programName + "/"
                + functionId.getName() + "(" + parameters.toString() + ")" + returnTypeCode);

        for (SimpleNode unwrappingNode : unwrapReferences) {
            TypeSpec parameterType = unwrappingNode.getTypeSpec();
            SymTabEntry parameterEntry = (SymTabEntry) unwrappingNode.getAttribute(ID);

            String upperTypeCode = null;
            String lowerTypeCode = null;
            String wrapTypeCode = null;

            if (parameterType == Predefined.integerType) {
                wrapTypeCode = "I";
                upperTypeCode = "I";
                lowerTypeCode = "i";
            }
            else if (parameterType == Predefined.realType) {
                wrapTypeCode = "R";
                upperTypeCode = "F";
                lowerTypeCode = "f";
            }
            else if (parameterType == Predefined.charType) {
                wrapTypeCode = "Ljava/lang/String;";
                upperTypeCode = "Ljava/lang/String;";
                lowerTypeCode = "Ljava/lang/String;";
            }
            else if (parameterType == Predefined.booleanType) {
                wrapTypeCode = "B";
                upperTypeCode = "Z";
                lowerTypeCode = "z";
            }

            int slot = parameterEntry.getIndex();
            if (parameterEntry.getSymTab().getNestingLevel() == 1) {
                slot++;
            }

            CodeGenerator.objectFile.println("    aload " + slot);
            CodeGenerator.objectFile.println("    getfield " + wrapTypeCode + "Wrap/value " + upperTypeCode);

            if (parameterEntry.getSymTab().getNestingLevel() == 1) {
                CodeGenerator.objectFile.println("    putstatic " + programName + "/"
                        + parameterEntry.getName() + " " + upperTypeCode);
            }
            else {
                CodeGenerator.objectFile.println("    " + lowerTypeCode + "load " + slot);
            }
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTvariableDeclaration node, Object data)
    {
        return data;
    }

    public Object visit(ASTparameter node, Object data)
    {
        return data;
    }

    public Object visit(ASTidentifier node, Object data)
    {
        SymTabEntry id = (SymTabEntry) node.getAttribute(ID);
        String fieldName = id.getName();
        TypeSpec type = id.getTypeSpec();
        String upperTypeCode = null;
        String lowerTypeCode = null;
        String wrapTypeCode = null;

        if (type == Predefined.integerType) {
            wrapTypeCode = "I";
            upperTypeCode = "I";
            lowerTypeCode = "i";
        }
        else if (type == Predefined.realType) {
            wrapTypeCode = "R";
            upperTypeCode = "F";
            lowerTypeCode = "f";
        }
        else if (type == Predefined.charType) {
            wrapTypeCode = "Ljava/lang/String;";
            upperTypeCode = "Ljava/lang/String;";
            lowerTypeCode = "Ljava/lang/String;"; // TODO: How to load a local variable string?
        }
        else if (type == Predefined.booleanType) {
            wrapTypeCode = "B";
            upperTypeCode = "Z";
            lowerTypeCode = "z";
        }

        // Emit the appropriate load instruction.
        if (id.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
            CodeGenerator.objectFile.println("    aload " + id.getIndex());
            CodeGenerator.objectFile.println("    getfield " + wrapTypeCode + "Wrap/value " + upperTypeCode);
        }
        else if (id.getSymTab().getNestingLevel() == 1) {
            CodeGenerator.objectFile.println("    getstatic " + programName +
                    "/" + fieldName + " " + upperTypeCode);
        }
        else {
            CodeGenerator.objectFile.println("    " + lowerTypeCode + "load " + id.getIndex());
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTintegerConstant node, Object data)
    {
        int value = (Integer) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTarray node, Object data)
    {
        SimpleNode arrayNode = (SimpleNode) node.jjtGetChild(0);
        TypeSpec type = arrayNode.getTypeSpec();

        return data;
    }

    public Object visit(ASTinterpretedString node, Object data)
    {
        String value = (String) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTrealConstant node, Object data)
    {
        float value = (Float) node.getAttribute(VALUE);

        // Emit a load constant instruction.
        CodeGenerator.objectFile.println("    ldc " + value);
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTprintStatement node, Object data)
    {
        CodeGenerator.objectFile.println("    getstatic java/lang/System/out Ljava/io/PrintStream;");
        CodeGenerator.objectFile.flush();

        SimpleNode printNode = (SimpleNode) node.jjtGetChild(0);
        TypeSpec type = printNode.getTypeSpec();
        String typeCode = null;
        printNode.jjtAccept(this, data);

        if (type == Predefined.integerType) {
            typeCode = "I";
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

        CodeGenerator.objectFile.println("    invokevirtual java/io/PrintStream/println(" + typeCode + ")V");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTblock node, Object data)
    {
        node.childrenAccept(this, data);

        // If the data is not the programName, that means I overwrote the data, so use it.
        // The data is always the programName unless you choose to send a child something else.
        if (data != programName) {
            String label = (String) data;
            CodeGenerator.objectFile.println("    goto " + label);
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTswitchBlock node, Object data)
    {
        // For now, a clone of the block visit method.
        // Looking to see if it needs any change at all.
        node.childrenAccept(this, programName);
        if (data != programName) {
            String label = (String) data;
            CodeGenerator.objectFile.println("    goto " + label);
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTforStatement node, Object data)
    {
        SimpleNode forClause = (SimpleNode) node.jjtGetChild(0);
        SimpleNode block = (SimpleNode) node.jjtGetChild(1);

        // If the for clause has 1 child, it is a while loop, so create a label
        if (forClause.jjtGetNumChildren() == 1) {
            String beginLabel = getNextLabel();
            CodeGenerator.objectFile.println(beginLabel + ":"); // Jump back here after each iteration
            CodeGenerator.objectFile.flush();
            String endLabel = (String) forClause.jjtAccept(this, data); // Jump to returned label when loop ends
            block.jjtAccept(this, data);
            CodeGenerator.objectFile.println("    goto " + beginLabel); // Restart loop
            CodeGenerator.objectFile.println(endLabel + ":"); // Jump here when the loop is done
        }
        else {
            ArrayList<Object> loopData = (ArrayList<Object>) forClause.jjtAccept(this, data);
            block.jjtAccept(this, data);
            ((Node) loopData.get(0)).jjtAccept(this, data); // Incrementing after running the body
            CodeGenerator.objectFile.println("    goto " + loopData.get(1));
            CodeGenerator.objectFile.println(loopData.get(2) + ":");
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTforClause node, Object data) {
        ArrayList<Object> loopData = new ArrayList<Object>();

        // If it has 1 child, it is a while loop
        if (node.jjtGetNumChildren() == 1) {
            return node.jjtGetChild(0).jjtAccept(this, data); // This returns a string label it will be jumping to
        }

        node.jjtGetChild(0).jjtAccept(this, data); // Perform the assignment, but don't include it as part of loop

        String beginLabel = getNextLabel(); // Prepare label for looping after the assignment has been done
        CodeGenerator.objectFile.println(beginLabel + ":");
        CodeGenerator.objectFile.flush();
        String endLabel = (String) node.jjtGetChild(1).jjtAccept(this, data); // Get label it will be jumping to in condition

        loopData.add(node.jjtGetChild(2)); // Delay incrementing until after the loop
        loopData.add(beginLabel);
        loopData.add(endLabel);

        return loopData;
    }

    public Object visit(ASTifStatement node, Object data)
    {
        SimpleNode condition = (SimpleNode) node.jjtGetChild(0);
        SimpleNode block = (SimpleNode) node.jjtGetChild(1);
        SimpleNode elseStatement = null;

        // Check if an else block exists
        if (node.jjtGetNumChildren() == 3) {
            elseStatement = (SimpleNode) node.jjtGetChild(2);
        }

        // When going into the condition, it will emit a conditional jump to the returned label
        String label = (String) condition.jjtAccept(this, data);
        String label2 = getNextLabel(); // If condition is true and there exist an else statement, jump to this label
        block.jjtAccept(this, label2); // Going in here will emit all the code in the body
        CodeGenerator.objectFile.println(label + ":"); // The label to jump to when the condition is false
        CodeGenerator.objectFile.flush();

        if (elseStatement != null) {
            elseStatement.jjtAccept(this, data);
        }

        CodeGenerator.objectFile.println(label2 + ":"); // Jump to this label if condition is true and there is an else
        CodeGenerator.objectFile.flush();

        return data;
    }

    // TODO: Figure out how to do this.
    public Object visit(ASTswitchStatement node, Object data)
    {
        SimpleNode switchVar = (SimpleNode) node.jjtGetChild(0);
        SimpleNode block = (SimpleNode) node.jjtGetChild(1);
        SimpleNode caseGroup = (SimpleNode) block.jjtGetChild(0);
        String defaultLabel = "defaultCaseLabel" + getNextLabel();

        ArrayList<SimpleNode> nodes = new ArrayList<SimpleNode>();

        for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
            nodes.add((SimpleNode) node.jjtGetChild(i));
        }

        /*

        ArrayList<ASTcaseGroup> cases = new ArrayList<ASTcaseGroup>();
        ASTdefaultCase defaultCase = null;
        // Not sure if this the way to do it yet.
        for (SimpleNode aNode : nodes) {
            if (aNode instanceof ASTcaseGroup) {
                ASTcaseGroup aCase = (ASTcaseGroup) aNode;
                // a Case can have multiple statements
                cases.add(aCase);
            }
            else if (aNode instanceof ASTdefaultCase) {
                defaultCase = (ASTdefaultCase) aNode;
            }
        }

        */

        return data;
    }

    public Object visit(ASTequalEqual node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    ifne " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTlessThan node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    ifge " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTgreaterThan node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    ifle " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTnotEqual node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    ifeq " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTlessEqual node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    ifgt " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTgreaterEqual node, Object data)
    {
        String label = getNextLabel();
        emitComparisonCode(node, data);
        CodeGenerator.objectFile.println("    iflt " + label);
        CodeGenerator.objectFile.flush();

        return label;
    }

    public Object visit(ASTincrement node, Object data)
    {
        SimpleNode incNode = (SimpleNode) node.jjtGetChild(0);
        TypeSpec type = node.getTypeSpec();

        SymTabEntry id = (SymTabEntry) incNode.getAttribute(ID);
        String fieldName = id.getName();

        String typePrefix = (type == Predefined.integerType) ? "i" : "f";
        String typePrefix2 = (type == Predefined.integerType) ? "I" : "F";

        CodeGenerator.objectFile.println("    ldc 1");
        CodeGenerator.objectFile.flush();

        incNode.jjtAccept(this, data);
        CodeGenerator.objectFile.println("    " + typePrefix + "add");
        CodeGenerator.objectFile.flush();

        if (id.getSymTab().getNestingLevel() == 1) {
            CodeGenerator.objectFile.println("    putstatic " + programName +
                    "/" + fieldName + " " + typePrefix2);
        }
        else {
            CodeGenerator.objectFile.println("    " + typePrefix + "store " + id.getIndex());
        }

        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTdecrement node, Object data)
    {
        SimpleNode decNode = (SimpleNode) node.jjtGetChild(0);
        TypeSpec type = node.getTypeSpec();

        String typePrefix = (type == Predefined.integerType) ? "i" : "f";
        String typePrefix2 = (type == Predefined.integerType) ? "I" : "F";

        SymTabEntry id = (SymTabEntry) decNode.getAttribute(ID);
        String fieldName = id.getName();

        CodeGenerator.objectFile.println("    ldc -1");
        CodeGenerator.objectFile.flush();

        decNode.jjtAccept(this, data);
        CodeGenerator.objectFile.println("    " + typePrefix + "add");
        CodeGenerator.objectFile.flush();

        if (id.getSymTab().getNestingLevel() == 1) {
            CodeGenerator.objectFile.println("    putstatic " + programName +
                    "/" + fieldName + " " + typePrefix2);
        }
        else {
            CodeGenerator.objectFile.println("    " + typePrefix + "store " + id.getIndex());
        }

        CodeGenerator.objectFile.flush();

        return data;
    }


    public Object visit(ASTadd node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        if (type != Predefined.charType) {
            // Emit code for the first expression
            // with type conversion if necessary.
            addend0Node.jjtAccept(this, data);
            if ((type == Predefined.realType) &&
                    (type0 == Predefined.integerType))
            {
                CodeGenerator.objectFile.println("    i2f");
                CodeGenerator.objectFile.flush();
            }

            // Emit code for the second expression
            // with type conversion if necessary.
            addend1Node.jjtAccept(this, data);
            if ((type == Predefined.realType) &&
                    (type1 == Predefined.integerType))
            {
                CodeGenerator.objectFile.println("    i2f");
                CodeGenerator.objectFile.flush();
            }

            // Emit the appropriate add instruction.
            CodeGenerator.objectFile.println("    " + typePrefix + "add");
            CodeGenerator.objectFile.flush();
        }
        else {
            String lhsType = null;
            String rhsType = null;

            if (type0 == Predefined.integerType) {
                lhsType = "I";
            }
            else if (type0 == Predefined.realType) {
                lhsType = "F";
            }
            else if (type0 == Predefined.charType) {
                lhsType = "Ljava/lang/String;";
            }

            if (type1 == Predefined.integerType) {
                rhsType = "I";
            }
            else if (type1 == Predefined.realType) {
                rhsType = "F";
            }
            else if (type1 == Predefined.charType) {
                rhsType = "Ljava/lang/String;";
            }

            CodeGenerator.objectFile.println("    new       java/lang/StringBuilder");
            CodeGenerator.objectFile.println("    dup");
            CodeGenerator.objectFile.println("    invokenonvirtual java/lang/StringBuilder/<init>()V");
            addend0Node.jjtAccept(this, data);
            CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/append("
                    + lhsType + ")Ljava/lang/StringBuilder;");
            addend1Node.jjtAccept(this, data);
            CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/append("
                    + rhsType + ")Ljava/lang/StringBuilder;");
            CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTbitwiseAnd node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        addend0Node.jjtAccept(this, data);
        addend1Node.jjtAccept(this, data);

        // Can only use bitwise operations on integers. What do we do when they're not integers? hmm...
        if (type0 == Predefined.integerType && type1 == Predefined.integerType) {
            // Emit the appropriate and instruction.
            CodeGenerator.objectFile.println("    " + "iand");
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTbitwiseOr node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        addend0Node.jjtAccept(this, data);
        addend1Node.jjtAccept(this, data);

        // Can only use bitwise operations on integers. What do we do when they're not integers? hmm...
        if (type0 == Predefined.integerType && type1 == Predefined.integerType) {
            // Emit the appropriate and instruction.
            CodeGenerator.objectFile.println("    " + "ior");
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTxor node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        addend0Node.jjtAccept(this, data);
        addend1Node.jjtAccept(this, data);

        // Can only use bitwise operations on integers. What do we do when they're not integers? hmm...
        if (type0 == Predefined.integerType && type1 == Predefined.integerType) {
            // Emit the appropriate and instruction.
            CodeGenerator.objectFile.println("    " + "ixor");
            CodeGenerator.objectFile.flush();
        }

        return data;
    }

    public Object visit(ASTsubtract node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "sub");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTmultiply node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "mul");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTdivide node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "div");
        CodeGenerator.objectFile.flush();

        return data;
    }

    public Object visit(ASTmodulo node, Object data)
    {
        SimpleNode addend0Node = (SimpleNode) node.jjtGetChild(0);
        SimpleNode addend1Node = (SimpleNode) node.jjtGetChild(1);

        TypeSpec type0 = addend0Node.getTypeSpec();
        TypeSpec type1 = addend1Node.getTypeSpec();

        // Get the addition type.
        TypeSpec type = node.getTypeSpec();
        String typePrefix = (type == Predefined.integerType) ? "i" : "f";

        // Emit code for the first expression
        // with type conversion if necessary.
        addend0Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type0 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit code for the second expression
        // with type conversion if necessary.
        addend1Node.jjtAccept(this, data);
        if ((type == Predefined.realType) &&
                (type1 == Predefined.integerType))
        {
            CodeGenerator.objectFile.println("    i2f");
            CodeGenerator.objectFile.flush();
        }

        // Emit the appropriate add instruction.
        CodeGenerator.objectFile.println("    " + typePrefix + "rem");
        CodeGenerator.objectFile.flush();

        return data;
    }
}