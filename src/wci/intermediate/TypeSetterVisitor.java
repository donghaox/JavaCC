package wci.intermediate;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

public class TypeSetterVisitor extends GoParserVisitorAdapter
{
    private void setType(SimpleNode node)
    {
        int i;
        int count = node.jjtGetNumChildren();
        TypeSpec type = Predefined.integerType;

        for (i = 0; i < count && type == Predefined.integerType; ++i) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            TypeSpec childType = child.getTypeSpec();
            
            if (childType == Predefined.realType) {
                type = Predefined.realType;
            }
            else if (childType == Predefined.charType) {
                type = Predefined.charType;
            }
            /* TODO: Haven't implemented handling booleans yet so I am leaving it commented
            else if (childType == Predefined.booleanType) {
                type = Predefined.booleanType;
            }
            */
        }

        for (; i < count && type == Predefined.realType; i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            TypeSpec childType = child.getTypeSpec();

            if (childType == Predefined.charType) {
                type = Predefined.charType;
            }
        }

        node.setTypeSpec(type);
    }
    
    public Object visit(ASTassignmentStatement node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTprintStatement node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTincrement node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }


    public Object visit(ASTdecrement node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTadd node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTsubtract node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTmultiply node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTdivide node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTmodulo node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTbitwiseAnd node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }


    public Object visit(ASTbitwiseOr node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTxor node, Object data)
    {
        Object obj = super.visit(node, data);
        setType(node);
        return obj;
    }

    public Object visit(ASTidentifier node, Object data)
    {
        return data;
    }
    
    public Object visit(ASTintegerConstant node, Object data)
    {
        return data;
    }
    
    public Object visit(ASTrealConstant node, Object data)
    {
        return data;
    }

    public Object visit(ASTarray node, Object data)
    {
        return data;
    }

    public Object visit(ASTbooleanConstant node, Object data)
    {
        return data;
    }

    public Object visit(ASTvoidConstant node, Object data)
    {
        return data;
    }
}
