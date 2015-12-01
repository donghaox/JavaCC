package wci.intermediate;

import wci.frontend.*;


public class ProlangParserVisitorAdapter implements ProlangParserVisitor {
    public Object visit(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    // TODO: As we change productions from #void to something else in Go.jjt, we will have to implement
    // TODO: each interface methods from the GoParserVisitor that is generated by JJTree.}

    public Object visit(ASTstatementList node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTassignmentStatement node, Object data) {
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

    public Object visit(ASTfunctionDeclaration node, Object data) {
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

    public Object visit(ASTswitchBlock node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTcaseGroup node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTforClause node, Object data) {
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

    public Object visit(ASTbitwiseAnd node, Object data) {
        return node.childrenAccept(this, data);
    }


    public Object visit(ASTbitwiseOr node, Object data) {
        return node.childrenAccept(this, data);
    }


    public Object visit(ASTxor node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTmodulo node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTvariableDeclaration node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTarray node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTarrayAssignmentStatement node, Object data) {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTprintln node, Object data) {
        return node.childrenAccept(this, data);
    }
    
	public Object visit(ASTprint node, Object data) {
		// TODO Auto-generated method stub
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

    public Object visit(ASTfunctionCall node, Object data) {
        return node.childrenAccept(this, data);
    }

	public Object visit(ASTif_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	public Object visit(ASTelse_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	public Object visit(ASTswitch_statement node, Object data) {
	      return node.childrenAccept(this, data);
	}

	public Object visit(ASTfor_statement node, Object data) {
	      return node.childrenAccept(this, data);
	} 
}
