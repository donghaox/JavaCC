package wci.backend.compiler;

import wci.frontend.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeKeyImpl;
import wci.intermediate.icodeimpl.ICodeNodeImpl;
import wci.intermediate.symtabimpl.*;

import java.util.ArrayList;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

public class CodeGeneratorVisitor extends ProlangParserVisitorAdapter implements ProlangParserTreeConstants
{
	private static String programName = null;
	private static int tagNumber = 0;

	public String getCurrentLabel() { return "label" + tagNumber; }
	public String getNextLabel() { return "label" + ++tagNumber; }


	public Object visit(ASTstatement_list node, Object data) {
		if (CodeGeneratorVisitor.programName == null) {
			CodeGeneratorVisitor.programName = (String) data;
		}
		return node.childrenAccept(this, data);
	}

	/*
	 * assignment statement
	 */
	public Object visit(ASTassignment_statement node, Object data)
	{
		String programName        = (String) data;
		SimpleNode variableNode   = get_child(node, 0);
		SimpleNode expressionNode = get_child(node, 1);

		SymTabEntry variableId = (SymTabEntry) variableNode.getAttribute(ID);
		String data_type = get_datatype(variableNode);
		String type_wrap = get_typewrap(variableNode);
		String type_des = get_typedes(variableNode);

		int_to_float(node, expressionNode);
		
		//handle expression
		if (variableId.getDefinition() != DefinitionImpl.REFERENCE_PARAMETER) {
			expressionNode.jjtAccept(this, data);
		}
		
		//get variable from program
		if (variableId.getSymTab().getNestingLevel() == 1) {
			CodeGenerator.objectFile.println("    putstatic " + programName +
					"/" + variableId.getName() + " " + type_des);
		}
		else if (variableId.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
			CodeGenerator.objectFile.println("    aload " + variableId.getIndex());
			expressionNode.jjtAccept(this, data);
			CodeGenerator.objectFile.println("    putfield " + type_wrap + "Wrap/value " + type_des);
		}
		//push variable
		else {
			if(data_type =="Ljava/lang/String;"){
				CodeGenerator.objectFile.println("    "  + "astore " + variableId.getIndex());
			}
			else if(data_type == "z"){
				CodeGenerator.objectFile.println("    "  + "istore " + variableId.getIndex());
			}
			else{
			CodeGenerator.objectFile.println("    " + data_type + "store " + variableId.getIndex());
			}
		}

		CodeGenerator.objectFile.flush();

		return data;
	}

	public Object visit(ASTfunction_declaration node, Object data) {
		return data;
	}

	public Object visit(ASTfunction_call node, Object data) {
		SymTabEntryImpl functionId = (SymTabEntryImpl) node.getAttribute(ID);
		SymTabImpl scope = (SymTabImpl) functionId.getAttribute(SymTabKeyImpl.ROUTINE_SYMTAB);
		ArrayList<SimpleNode> unwrapReferences = new ArrayList<SimpleNode>();
		String returnTypeCode = get_typedes(node);
		StringBuilder parameters = new StringBuilder();

		for (SymTabEntry parameter : scope.values()) {
			if (parameter.getDefinition() != DefinitionImpl.FUNCTION) {
				int index = parameter.getIndex();
				SimpleNode param = (SimpleNode) node.jjtGetChild(index + 1);
				TypeSpec parameterType = param.getTypeSpec();
				SymTabEntry parameterEntry = (SymTabEntry) param.getAttribute(ID);

				String type_des = get_typedes(param);
				String data_type = get_datatype(param);
				String type_wrap = get_typewrap(param);
				
				if (parameterType == Predefined.integerType) {

					if (parameter.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
						parameters.append("LIWrap;");
					}
					else {
						parameters.append("I");
					}
				}
				else if (parameterType == Predefined.realType) {
					if (parameter.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
						parameters.append("LRWrap;");
					}
					else {
						parameters.append("F");
					}
				}
				else if (parameterType == Predefined.charType) {
					parameters.append("Ljava/lang/String;"); 
				}
				else if (parameterType == Predefined.booleanType) {
					if (parameter.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
						parameters.append("LBWrap;");
					}
					else {
						parameters.append("Z");
					}
				}

				if (parameter.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
					CodeGenerator.objectFile.println("    new " + type_wrap + "Wrap ");
					CodeGenerator.objectFile.println("    dup");
					CodeGenerator.objectFile.flush();
				}

				if (parameterEntry == null) {
					param.jjtAccept(this, data);
				}
				else if (functionId.getSymTab().getNestingLevel() == 1) {
					CodeGenerator.objectFile.println("    getstatic " + programName +
							"/" + parameterEntry.getName() + " " + type_des);
					CodeGenerator.objectFile.flush();
				}
				else {
					int slot = parameterEntry.getIndex();
					if (parameterEntry.getSymTab().getNestingLevel() == 1) {
						slot++;
					}
					CodeGenerator.objectFile.println("    " + data_type + "load " + slot);
					CodeGenerator.objectFile.flush();
				}

				if (parameter.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
					CodeGenerator.objectFile.println("    invokenonvirtual " + type_wrap
							+ "Wrap/<init>(" + type_des + ")" + returnTypeCode);
					CodeGenerator.objectFile.println("    dup");
					int slot = parameterEntry.getIndex();
					if (parameterEntry.getSymTab().getNestingLevel() == 1) {
						slot++;
					}
					CodeGenerator.objectFile.println("    astore " + slot);
					CodeGenerator.objectFile.flush();
					unwrapReferences.add(param);
				}
			}
		}

		CodeGenerator.objectFile.println("    invokestatic  " + programName + "/"
				+ functionId.getName() + "(" + parameters.toString() + ")" + returnTypeCode);

		for (SimpleNode unwrappingNode : unwrapReferences) {
			SymTabEntry parameterEntry = (SymTabEntry) unwrappingNode.getAttribute(ID);

			String type_des = get_typedes(unwrappingNode);
			String data_type = get_datatype(unwrappingNode);
			String type_wrap = get_typewrap(unwrappingNode);

			int slot = parameterEntry.getIndex();
			if (parameterEntry.getSymTab().getNestingLevel() == 1) {
				slot++;
			}

			CodeGenerator.objectFile.println("    aload " + slot);
			CodeGenerator.objectFile.println("    getfield " + type_wrap + "Wrap/value " + type_des);

			if (parameterEntry.getSymTab().getNestingLevel() == 1) {
				CodeGenerator.objectFile.println("    putstatic " + programName + "/"
						+ parameterEntry.getName() + " " + type_des);
			}
			else {
				CodeGenerator.objectFile.println("    " + data_type + "load " + slot);
			}
		}

		CodeGenerator.objectFile.flush();

		return data;
	}

	public Object visit(ASTvariable_declaration node, Object data)
	{
		return data;
	}

	public Object visit(ASTparameter node, Object data)
	{
		return data;
	}

	/*
	 * load up identifier
	 */
	public Object visit(ASTidentifier node, Object data)
	{
		SymTabEntry entry = (SymTabEntry) node.getAttribute(ID);
		String type_des = get_typedes(node);
		String data_type = get_datatype(node);
		String type_wrap = get_typewrap(node);

		//push program variable to stack for declared variable
		if (entry.getSymTab().getNestingLevel() == 1) {
			CodeGenerator.objectFile.println("    getstatic " + programName +
					"/" + entry.getName() + " " + type_des);
		}
		//handle passing by reference using BWrap, IWrap, RWrap, CWrap
		else if(entry.getDefinition() == DefinitionImpl.REFERENCE_PARAMETER) {
			CodeGenerator.objectFile.println("    aload " + entry.getIndex());
			CodeGenerator.objectFile.println("    getfield " + type_wrap + "Wrap/value " + type_des);
		}
		//use index to load according variable to stack, first time declare
		else {
			CodeGenerator.objectFile.println("    " + data_type + "load " + entry.getIndex());
		}

		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for integer
	 */
	public Object visit(ASTintegerConstant node, Object data)
	{
		int _integer = (Integer) node.getAttribute(VALUE);
		CodeGenerator.objectFile.println("    ldc " + _integer);
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for integer
	 */
	public Object visit(ASTbooleanConstant node, Object data)
	{
		int _integer = (Integer) node.getAttribute(VALUE);
		CodeGenerator.objectFile.println("    ldc " + _integer);
		CodeGenerator.objectFile.flush();

		return data;
	}
	/*
	 * generate jasmin for string
	 */
	public Object visit(ASTprolang_string node, Object data)
	{
		String _string = (String) node.getAttribute(VALUE);
		CodeGenerator.objectFile.println("    ldc " + _string);
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for decimal number
	 */
	public Object visit(ASTrealConstant node, Object data)
	{
		float _float = (Float) node.getAttribute(VALUE);
		CodeGenerator.objectFile.println("    ldc " + _float);
		CodeGenerator.objectFile.flush();

		return data;
	}

	/* 
	 * Generate jasmin for println
	 */
	public Object visit(ASTprintln node, Object data)
	{
		SimpleNode target = get_child(node, 0);
		String type_descriptor = get_typedes(target);

		CodeGenerator.objectFile.println("    getstatic java/lang/System/out Ljava/io/PrintStream;");
		CodeGenerator.objectFile.flush();
		target.jjtAccept(this, data);
		CodeGenerator.objectFile.println("    invokevirtual java/io/PrintStream/println(" + type_descriptor + ")V");
		CodeGenerator.objectFile.flush();

		return data;
	}

	/* 
	 * Generate jasmin for print
	 */
	public Object visit(ASTprint node, Object data)
	{
		SimpleNode target = get_child(node, 0);
		String type_descriptor = get_typedes(target);

		CodeGenerator.objectFile.println("    getstatic java/lang/System/out Ljava/io/PrintStream;");
		CodeGenerator.objectFile.flush();
		target.jjtAccept(this, data);
		CodeGenerator.objectFile.println("    invokevirtual java/io/PrintStream/print(" + type_descriptor + ")V");
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

	/*
	 * generate jasmin for for statement
	 */
	public Object visit(ASTfor_statement node, Object data)
	{
		//node_0 = for loop header, node_1 = blockcode
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 = get_child(node, 1);

		ArrayList<Object> for_clause = (ArrayList<Object>) node_0.jjtAccept(this, data);
		SimpleNode for_clause_data_0 = (SimpleNode) for_clause.get(0);
		String start_location =  (String) for_clause.get(1);
		String end_location = (String) for_clause.get(2);
		
		//execute body and increment/decrement
		node_1.jjtAccept(this, data);
		for_clause_data_0.jjtAccept(this, data); 
		
		//afterthought
		CodeGenerator.objectFile.println("    goto " + start_location);
		CodeGenerator.objectFile.println(end_location + ":");

		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for for_header
	 */
	public Object visit(ASTfor_header node, Object data) {
		ArrayList<Object> for_loop = new ArrayList<Object>();

		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 = get_child(node, 1);
		SimpleNode node_2 = get_child(node, 2);
		
		//initialization, get start location
		String start_location = getNextLabel();
		node_0.jjtAccept(this, data);
		
		//afterthought
		CodeGenerator.objectFile.println(start_location + ":");
		CodeGenerator.objectFile.flush();
		String end_location = (String) node_1.jjtAccept(this, data); 

		for_loop.add(node_2);
		for_loop.add(start_location);
		for_loop.add(end_location);

		return for_loop;
	}

	/*
	 * generate jasmin for if statement
	 */
	public Object visit(ASTif_statement node, Object data)
	{
		//node0 = expression, node1 = blockcode, node2 = else if exitst
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 = get_child(node, 1);
		SimpleNode node_2 = get_child(node, 2);

		String true_false = (String) node_0.jjtAccept(this, data);
		//if true
		String if_true = getNextLabel(); 
		node_1.jjtAccept(this, if_true); 
		CodeGenerator.objectFile.println(if_true + ":");
		CodeGenerator.objectFile.flush();
		//else
		CodeGenerator.objectFile.println(true_false + ":"); 
		CodeGenerator.objectFile.flush();
		//executing else block if exists
		if (node_2 != null) {
			node_2.jjtAccept(this, data);
		}

		return data;
	}

	public Object visit(ASTequalEqual node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    ifne " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	public Object visit(ASTlessThan node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    ifge " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	public Object visit(ASTgreaterThan node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    ifle " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	public Object visit(ASTnotEqual node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    ifeq " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	public Object visit(ASTlessEqual node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    ifgt " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	public Object visit(ASTgreaterEqual node, Object data)
	{
		String label = getNextLabel();
		jasmin_compare(node, data);
		CodeGenerator.objectFile.println("    iflt " + label);
		CodeGenerator.objectFile.flush();

		return label;
	}

	/*
	 * generate jasmin for addition
	 */
	public Object visit(ASTadd node, Object data)
	{
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);

		TypeSpec type = node.getTypeSpec();
		String typePrefix = (type == Predefined.integerType) ? "i" : "f";

		// adding two strings
		if  (type == Predefined.charType) {
			String a = get_typedes(node_0);
			String b = get_typedes(node_0);

			CodeGenerator.objectFile.println("    new       java/lang/StringBuilder");
			CodeGenerator.objectFile.println("    dup");
			CodeGenerator.objectFile.println("    invokenonvirtual java/lang/StringBuilder/<init>()V");
			node_0.jjtAccept(this, data);
			CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/append("
					+ a + ")Ljava/lang/StringBuilder;");
			node_1.jjtAccept(this, data);
			CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/append("
					+ b + ")Ljava/lang/StringBuilder;");
			CodeGenerator.objectFile.println("    invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");
			CodeGenerator.objectFile.flush();
		}

		//adding two numbers
		else {
			node_0.jjtAccept(this, data);
			int_to_float(node, node_0);
			node_1.jjtAccept(this, data);
			int_to_float(node, node_1);
			CodeGenerator.objectFile.println("    " + typePrefix + "add");
			CodeGenerator.objectFile.flush();
		}
		
		return data;
	}

	/*
	 * generate jasmin for subtraction
	 */
	public Object visit(ASTsubtract node, Object data)
	{
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);
		String type = get_datatype(node);

		node_0.jjtAccept(this, data);
		int_to_float(node, node_0);
		node_1.jjtAccept(this, data);
		int_to_float(node, node_1);

		CodeGenerator.objectFile.println("    " + type + "sub");
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for multiplication
	 */
	public Object visit(ASTmultiply node, Object data)
	{
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);
		String type = get_datatype(node);

		node_0.jjtAccept(this, data);
		int_to_float(node, node_0);
		node_1.jjtAccept(this, data);
		int_to_float(node, node_1);

		CodeGenerator.objectFile.println("    " + type + "mul");
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for mod
	 */
	public Object visit(ASTmodulo node, Object data)
	{
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);
		String type = get_datatype(node);

		node_0.jjtAccept(this, data);
		int_to_float(node, node_0);
		node_1.jjtAccept(this, data);
		int_to_float(node, node_1);

		CodeGenerator.objectFile.println("    " + type + "rem");
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * generate jasmin for division
	 */
	public Object visit(ASTdivide node, Object data)
	{
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);
		String type = get_datatype(node);

		node_0.jjtAccept(this, data);
		int_to_float(node, node_0);
		node_1.jjtAccept(this, data);
		int_to_float(node, node_1);

		CodeGenerator.objectFile.println("    " + type + "div");
		CodeGenerator.objectFile.flush();

		return data;
	}

	/*
	 * Helper get_child method
	 */
	private SimpleNode get_child(SimpleNode _node, int child){
		if(child <= _node.jjtGetNumChildren() - 1){
			return (SimpleNode) _node.jjtGetChild(child);
		}
		else{
			return null;
		}
	}

	/*
	 * Helper comparison method
	 */
	public void jasmin_compare(SimpleNode node, Object data) {
		SimpleNode node_0 = get_child(node, 0);
		SimpleNode node_1 =  get_child(node, 1);

		//load node0, convert it to float if necessary
		node_0.jjtAccept(this, data);
		if (node_0.getTypeSpec() == Predefined.integerType) {
			CodeGenerator.objectFile.println("    i2f");
			CodeGenerator.objectFile.flush();
		}

		//load node1, convert it to float if necessary
		node_1.jjtAccept(this, data);
		if (node_1.getTypeSpec() == Predefined.integerType) {
			CodeGenerator.objectFile.println("    i2f");
			CodeGenerator.objectFile.flush();
		}
		CodeGenerator.objectFile.println("    fcmpg");
	}

	/*
	 * Helper method get data type
	 */
	public String get_datatype(SimpleNode node){
		if(node.getTypeSpec() == Predefined.integerType){
			return "i";
		}
		else if(node.getTypeSpec() == Predefined.realType){
			return "f";
		}
		else if(node.getTypeSpec() == Predefined.charType){
			return "Ljava/lang/String;";
		}
		else if(node.getTypeSpec() == Predefined.booleanType){
			return "z";
		}
		else{
			return null;
		}
	}

	/*
	 * Helper method get type descriptor
	 */
	public static String get_typedes(SimpleNode node){
		if(node.getTypeSpec() == Predefined.integerType){
			return "I";
		}
		else if(node.getTypeSpec() == Predefined.realType){
			return "F";
		}
		else if(node.getTypeSpec() == Predefined.charType){
			return "Ljava/lang/String;";
		}
		else if(node.getTypeSpec() == Predefined.booleanType){
			return "Z";
		}
		else if (node.getTypeSpec() == Predefined.voidType) {
			return "V";
		}
		else{
			return null;
		}
	}

	/*
	 * Helper method get type wrap for BWrap, CWrap,IWrap
	 */
	public String get_typewrap(SimpleNode node){
		if(node.getTypeSpec() == Predefined.integerType){
			return "I";
		}
		else if(node.getTypeSpec() == Predefined.realType){
			return "R";
		}
		else if(node.getTypeSpec() == Predefined.charType){
			return "Ljava/lang/String;";
		}
		else if(node.getTypeSpec() == Predefined.booleanType){
			return "B";
		}
		else{
			return null;
		}
	}

	/*
	 * Helper method convert integer to float
	 */
	public boolean int_to_float(SimpleNode root, SimpleNode child){
		if ((root == Predefined.realType) &&
				(child.getTypeSpec() == Predefined.integerType))
		{
			CodeGenerator.objectFile.println("    i2f");
			CodeGenerator.objectFile.flush();
			return true;
		}
		else{
			return false;
		}
	}
}