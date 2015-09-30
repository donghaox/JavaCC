package wci.backend.interpreter.executors;

import java.util.*;

import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;
import wci.backend.interpreter.*;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.backend.interpreter.RuntimeErrorCode.*;

/**
 * <h1>ExpressionExecutor</h1>
 *
 * <p>Execute an expression.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class ExpressionExecutor extends StatementExecutor
{
    /**
     * Constructor.
     * @param parent executor.
     */
    public ExpressionExecutor(Executor parent)
    {
        super(parent);
    }

    /**
     * Execute an expression.
     * @param node the root intermediate code node of the compound statement.
     * @return the computed value of the expression.
     */
    public Object execute(ICodeNode node)
    {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        switch (nodeType) {

            case VARIABLE: {

                // Get the variable's symbol table entry and return its value.
                SymTabEntry entry = (SymTabEntry) node.getAttribute(ID);
                return entry.getAttribute(DATA_VALUE);
            }

            case INTEGER_CONSTANT: {
                // Return the integer value.
                return (Integer) node.getAttribute(VALUE);
            }

            case REAL_CONSTANT: {

                // Return the float value.
                return (Float) node.getAttribute(VALUE);
            }

            case STRING_CONSTANT: {

                // Return the string value.
                return (String) node.getAttribute(VALUE);
            }

            case SET:{
                //strToTreeSet(node);
                setUpSet(node);
                return (TreeSet<Integer>)node.getAttribute(VALUE);
            }

            case NEGATE: {

                // Get the NEGATE node's expression node child.
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                // Execute the expression and return the negative of its value.
                Object value = execute(expressionNode);
                if (value instanceof Integer) {
                    return -((Integer) value);
                }
                else {
                    return -((Float) value);
                }
            }

            case NOT: {

                // Get the NOT node's expression node child.
                ArrayList<ICodeNode> children = node.getChildren();
                ICodeNode expressionNode = children.get(0);

                // Execute the expression and return the "not" of its value.
                boolean value = (Boolean) execute(expressionNode);
                return !value;
            }

            // Must be a binary operator.
            default: return executeBinaryOperator(node, nodeType);
        }
    }

    // Set of arithmetic operator node types.
    private static final EnumSet<ICodeNodeTypeImpl> ARITH_OPS =
        EnumSet.of(ADD, SUBTRACT, MULTIPLY, FLOAT_DIVIDE, INTEGER_DIVIDE, MOD,SET_RANGE);

    /**
     * Execute a binary operator.
     * @param node the root node of the expression.
     * @param nodeType the node type.
     * @return the computed value of the expression.
     */
    private Object executeBinaryOperator(ICodeNode node,
                                         ICodeNodeTypeImpl nodeType)
    {
        // Get the two operand children of the operator node.
        ArrayList<ICodeNode> children = node.getChildren();
        ICodeNode operandNode1 = children.get(0);
        ICodeNode operandNode2 = children.get(1);

        // Operands.
        Object operand1 = execute(operandNode1);
        Object operand2 = execute(operandNode2);

        boolean integerMode = (operand1 instanceof Integer) &&
                              (operand2 instanceof Integer);
        boolean setMode = (operand1 instanceof  Set || operand1 instanceof Integer &&
                           operand2 instanceof  Set);

        // ====================
        // Arithmetic operators
        // ====================

        if (ARITH_OPS.contains(nodeType)) {
            if (integerMode) {
                int value1 = (Integer) operand1;
                int value2 = (Integer) operand2;

                // Integer operations.
                switch (nodeType) {
                    //new here SET_RANGE
                    case SET_RANGE:{
                        ArrayList<Integer> list = new ArrayList<>();
                        while (value1 <= value2){
                            list.add(value1);
                            value1++;
                        }
                        return list;
                    }
                    case ADD:      return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;

                    case FLOAT_DIVIDE: {

                        // Check for division by zero.
                        if (value2 != 0) {
                            return ((float) value1)/((float) value2);
                        }
                        else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }

                    case INTEGER_DIVIDE: {

                        // Check for division by zero.
                        if (value2 != 0) {
                            return value1/value2;
                        }
                        else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }

                    case MOD:  {

                        // Check for division by zero.
                        if (value2 != 0) {
                            return value1%value2;
                        }
                        else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0;
                        }
                    }
                }
            }else if (setMode){
                //my new set binary operations
                Set<Integer> value1 = (TreeSet<Integer>)operand1;
                Set<Integer> value2 = (TreeSet<Integer>)operand2;
                switch (nodeType){
                    case MULTIPLY:return intersection(value1, value2);
                    case ADD: return union(value1,value2);
                    case SUBTRACT: return difference(value1, value2);
                }
            }
            else {
                float value1 = operand1 instanceof Integer
                                   ? (Integer) operand1 : (Float) operand1;
                float value2 = operand2 instanceof Integer
                                   ? (Integer) operand2 : (Float) operand2;

                // Float operations.
                switch (nodeType) {
                    case ADD:      return value1 + value2;
                    case SUBTRACT: return value1 - value2;
                    case MULTIPLY: return value1 * value2;

                    case FLOAT_DIVIDE: {

                        // Check for division by zero.
                        if (value2 != 0.0f) {
                            return value1/value2;
                        }
                        else {
                            errorHandler.flag(node, DIVISION_BY_ZERO, this);
                            return 0.0f;
                        }
                    }
                }
            }
        }

        // ==========
        // AND and OR
        // ==========

        else if ((nodeType == AND) || (nodeType == OR)) {
            boolean value1 = (Boolean) operand1;
            boolean value2 = (Boolean) operand2;

            switch (nodeType) {
                case AND: return value1 && value2;
                case OR:  return value1 || value2;
            }
        }

        // ====================
        // Relational operators
        // ====================

        else if (integerMode) {
            int value1 = (Integer) operand1;
            int value2 = (Integer) operand2;

            // Integer operands.
            switch (nodeType) {
                case EQ: return value1 == value2;
                case NE: return value1 != value2;
                case LT: return value1 <  value2;
                case LE: return value1 <= value2;
                case GT: return value1 >  value2;
                case GE: return value1 >= value2;
            }
        }
        else if(setMode) {
            //mew set relational operators
            Set<Integer> value1, value2;
            if(operand1 instanceof Integer){
                value1 = new TreeSet<>();
                value1.add((Integer)operand1);
            } else
            value1 = (TreeSet<Integer>)operand1;
            value2 = (TreeSet<Integer>)operand2;

            switch (nodeType) {
                case EQ: return (value1.containsAll(value2) && value2.containsAll(value1));
                case LE: return isSubset(value1, value2);
                case GE: return isSuperset(value1, value2);
                case NE: return !value1.equals(value2);
                case IN_CODE: {
                    return  value2.containsAll(value1);
                }

            }
        }else {
                float value1 = operand1 instanceof Integer
                        ? (Integer) operand1 : (Float) operand1;
                float value2 = operand2 instanceof Integer
                        ? (Integer) operand2 : (Float) operand2;

                // Float operands.
                switch (nodeType) {
                    case EQ: return value1 == value2;
                    case NE: return value1 != value2;
                    case LT: return value1 <  value2;
                    case LE: return value1 <= value2;
                    case GT: return value1 >  value2;
                    case GE: return value1 >= value2;
                }
            }

        return 0;  // should never get here
    }

    private void strToTreeSet(ICodeNode node){
        String str;

        if (node.getAttribute(VALUE) instanceof Set) {
            return;
        }
        else
        str = (String)node.getAttribute(VALUE);
        SymTabEntry entry;
        String[] parts = str.split(",");
        String[] temp;
        int n1;
        int n2;
        int i;
        Set<Integer> valueSet = new TreeSet<>();
        for ( String e : parts){
            e = e.trim();
            if (e.matches("\\d+")) {
                valueSet.add(Integer.parseInt(e));
            }else if (e.matches("\\w+")){
                entry = symTabStack.getLocalSymTab().lookup(e);
                i = (int)entry.getAttribute(DATA_VALUE);
                valueSet.add(i);
            }
            else if (e.contains("..")) {
                 temp = e.split("\\.\\.");
                if (temp[0].matches("[a-zA-Z]\\w*")){
                    entry = symTabStack.getLocalSymTab().lookup(temp[0]);
                    n1 = (int)entry.getAttribute(DATA_VALUE);
                }else
                n1 = Integer.parseInt(temp[0]);
                if(temp[1].matches("[a-zA-Z]\\w*")){
                    entry = symTabStack.getLocalSymTab().lookup(temp[1]);
                    n2 = (int)entry.getAttribute(DATA_VALUE);
                }else
                n2 = Integer.parseInt(temp[1]);
                while(n1 <= n2){
                    valueSet.add(n1);
                    n1++;
                }
            }else if(e.matches("\\w+\\*\\w+")){
                temp = e.split("\\*");
                if (temp[0].matches("[a-zA-Z]\\w*"))
                    n1 = idToInt(temp[0]);
                else
                    n1 = Integer.parseInt(temp[0]);

                if (temp[1].matches("[a-zA-Z]\\w*"))
                    n2 = idToInt(temp[1]);
                else
                    n2 = Integer.parseInt(temp[1]);

                valueSet.add(n1*n2);
            }
        }
        node.setAttribute(VALUE, valueSet);
    }

    private int idToInt(String e){
        SymTabEntry entry = symTabStack.getLocalSymTab().lookup(e);
        return (int)entry.getAttribute(DATA_VALUE);
    }

    public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>();
        for (T x : setA)
            if (setB.contains(x))
                tmp.add(x);
        return tmp;
    }

    public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
        Set<T> tmpA;
        Set<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
        return setB.containsAll(setA);
    }

    public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
        return setA.containsAll(setB);
    }

    protected void setUpSet(ICodeNode node){
        Set<Integer> valueSet;
        valueSet = new TreeSet<>();
        ArrayList<ICodeNode> children = node.getChildren();
/*        children.stream().forEach(i ->{
            if(execute(i) instanceof Integer)
            valueSet.add((Integer)execute(i));
            else {
                valueSet.addAll((ArrayList<Integer>)ex)
            }
        });*/
        Object result;
        for (ICodeNode i : children){
            result = execute(i);
            if (result instanceof Integer){
                valueSet.add((Integer)result);
            }
            else {
                ArrayList<Integer> list = (ArrayList<Integer>)result;
                valueSet.addAll(list);
            }
        }


        node.setAttribute(VALUE, valueSet);
    }

}
