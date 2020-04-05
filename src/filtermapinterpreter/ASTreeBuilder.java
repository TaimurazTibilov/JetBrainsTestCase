package filtermapinterpreter;

import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.List;

public class ASTreeBuilder extends AbstractParser {
    public static final String INT_OPERATORS = "+-*";
    public static final String BOOL_OPERATORS = "&|";
    public static final String COMP_OPERATORS = "><=";

    private List<ASTNode> elements = new ArrayList<>();

    public ASTreeBuilder(String source) {
        super(source);
    }

    private OutputType getOperationType(String operator) {
        if (INT_OPERATORS.contains(operator))
            return OutputType.INT;
        if (BOOL_OPERATORS.contains(operator) || COMP_OPERATORS.contains(operator))
            return OutputType.BOOLEAN;
        return OutputType.NONE;
    }

    private boolean checkOperands(ASTNode first, ASTNode second, ASTNode operator) {
        if (COMP_OPERATORS.contains(operator.value) || INT_OPERATORS.contains(operator.value))
            return first.outputType == OutputType.INT && second.outputType == OutputType.INT;
        else {
            if (BOOL_OPERATORS.contains(operator.value))
                return first.outputType == OutputType.BOOLEAN && second.outputType == OutputType.BOOLEAN;
        }
        return false;
    }

    private ASTNode getNumber() {
        String num = matchTo(NodeType.NUMBER);
        return new ASTNode(num, OutputType.INT, NodeType.NUMBER);
    }

    private ASTNode getElement() {
        String elem = matchTo(NodeType.ELEMENT);
        ASTNode res = new ASTNode(elem, OutputType.INT, NodeType.ELEMENT);
        elements.add(res);
        return res;
    }

    private ASTNode getOperation() {
        String operation = matchTo(NodeType.OPERATION);
        return new ASTNode(operation, getOperationType(operation), NodeType.OPERATION);
    }

    private ASTNode getExpression() throws InvalidSyntaxException, InvalidTypeException {
        if (isMatchesTo(NodeType.ELEMENT))
            return getElement();
        if (isMatchesTo(NodeType.NUMBER))
            return getNumber();
        if (!matchTo('('))
            throw new InvalidSyntaxException("Expected expression!");
        ASTNode leftExpression = getExpression();
        ASTNode operator = getOperation();
        ASTNode rightExpression = getExpression();
        if (!checkOperands(leftExpression, rightExpression, operator))
            throw new InvalidTypeException("Wrong type of operands!");
        if (!matchTo(')'))
            throw new InvalidSyntaxException("Expected closing bracket ')'!");
        operator.addChild(leftExpression);
        operator.addChild(rightExpression);
        return operator;
    }

    public ASTree build() throws InvalidSyntaxException, InvalidTypeException {
        if (isMatchesTo(NodeType.MAP_EXPRESSION)) {
            matchTo(NodeType.MAP_EXPRESSION);
            if (!matchTo('{'))
                throw new InvalidSyntaxException("Expected opening bracket '{'!");
            ASTNode root = getExpression();
            if (!matchTo('}'))
                throw new InvalidSyntaxException("Expected closing bracket '}'!");
            if (root.outputType != OutputType.INT)
                throw new InvalidTypeException("Expected integer output type!");
            return new ASTree(root, elements, NodeType.MAP_EXPRESSION);
        } else {
            if (isMatchesTo(NodeType.FILTER_EXPRESSION)) {
                matchTo(NodeType.FILTER_EXPRESSION);
                if (!matchTo('{'))
                    throw new InvalidSyntaxException("Expected opening bracket '{'!");
                ASTNode root = getExpression();
                if (!matchTo('}'))
                    throw new InvalidSyntaxException("Expected closing bracket '}'!");
                if (root.outputType != OutputType.BOOLEAN)
                    throw new InvalidTypeException("Expected integer output type!");
                return new ASTree(root, elements, NodeType.FILTER_EXPRESSION);
            }
        }
        throw new InvalidSyntaxException("Cannot match to any call-expression!");
    }

    public static ASTree buildDefaultFilter() {
        ASTNode first = new ASTNode("1", OutputType.INT, NodeType.NUMBER);
        ASTNode second = new ASTNode("1", OutputType.INT, NodeType.NUMBER);
        ASTNode operator = new ASTNode("=", OutputType.BOOLEAN, NodeType.OPERATION);
        operator.addChild(first);
        operator.addChild(second);
        return new ASTree(operator, NodeType.FILTER_EXPRESSION);
    }

    public static ASTree buildDefaultMap() {
        ASTNode element = new ASTNode("element", OutputType.INT, NodeType.ELEMENT);
        ArrayList<ASTNode> elList = new ArrayList<>();
        elList.add(element);
        return new ASTree(element, elList, NodeType.MAP_EXPRESSION);
    }

    public static ArrayList<ASTree> rebuildToFilterMap(ArrayList<ASTree> calls) throws InvalidTypeException {
        ASTree map = null;
        ASTree filter = null;
        for (ASTree call : calls) {
            switch (call.getRootType()) {
                case MAP_EXPRESSION:
                    if (map != null)
                        map.mergeTo(call);
                    map = call;
                    break;
                case FILTER_EXPRESSION:
                    if (map != null)
                        map.mergeTo(call);
                    filter = ASTree.combine(filter, call);
                    break;
                default:
                    throw new InvalidTypeException("Expected map or filter call!");
            }
        }
        ArrayList<ASTree> result = new ArrayList<>();
        result.add(filter);
        result.add(map);
        return result;
    }
}
