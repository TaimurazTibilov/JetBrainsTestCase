package filtermapinterpreter;

import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class - builder of an abstract syntax tree (AST) by grammar rules given in the case.
 * It could build only Map- or Filter-expressions, that is needed in this case.
 *
 * @author Taimuraz Tibilov
 */
public class ASTreeBuilder extends AbstractParser {

    public static final String INT_OPERATORS = "+-*"; // Operators with integer operands and output
    public static final String BOOL_OPERATORS = "&|"; // Operators with boolean operands and output
    public static final String COMP_OPERATORS = "><="; // Operators with integer operands but boolean output

    private List<ASTNode> elements = new ArrayList<>(); // List of nodes of ELEMENT type, replaces if needed to merge

    /**
     * Constructor
     *
     * @param source call expression to parse. If got null or empty - do nothing.
     */
    public ASTreeBuilder(String source) {
        super(source);
    }

    /**
     * Helper function, matches each operator to its output type
     *
     * @param operator operator to match to
     * @return output type of given operator
     */
    private OutputType getOperationType(String operator) {
        if (operator.length() == 1) {
            if (INT_OPERATORS.contains(operator))
                return OutputType.INT;
            if (BOOL_OPERATORS.contains(operator) || COMP_OPERATORS.contains(operator))
                return OutputType.BOOLEAN;
        }
        return OutputType.NONE;
    }

    /**
     * Helper function, check each operands type to operation type
     *
     * @param first    1st operand
     * @param second   2nd operand
     * @param operator binary operator
     * @return true if type of each operand matches to operator, false otherwise
     */
    private boolean checkOperands(ASTNode first, ASTNode second, ASTNode operator) {
        if (COMP_OPERATORS.contains(operator.value) || INT_OPERATORS.contains(operator.value))
            return first.outputType == OutputType.INT && second.outputType == OutputType.INT;
        else {
            if (BOOL_OPERATORS.contains(operator.value))
                return first.outputType == OutputType.BOOLEAN && second.outputType == OutputType.BOOLEAN;
        }
        return false;
    }

    /**
     * Getter of the NUMBER node
     *
     * @return NUMBER node of the AST
     */
    private ASTNode getNumber() {
        String num = matchTo(NodeType.NUMBER);
        return new ASTNode(num, OutputType.INT, NodeType.NUMBER);
    }

    /**
     * Getter of the ELEMENT node, adds node to the {@code elements} List
     *
     * @return ELEMENT node of the AST
     */
    private ASTNode getElement() {
        String elem = matchTo(NodeType.ELEMENT);
        ASTNode res = new ASTNode(elem, OutputType.INT, NodeType.ELEMENT);
        elements.add(res);
        return res;
    }

    /**
     * Getter of the OPERATOR node
     *
     * @return OPERATOR node of the AST
     */
    private ASTNode getOperation() {
        String operation = matchTo(NodeType.OPERATION);
        return new ASTNode(operation, getOperationType(operation), NodeType.OPERATION);
    }

    /**
     * Parse, build and check expressions - element, number or binary. Main builder of this class.
     *
     * @return root node of the AST (expression inside call)
     * @throws InvalidSyntaxException if cannot parse expected expression (source has syntax error)
     * @throws InvalidTypeException   if cannot match output types between operators and operands
     */
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

    /**
     * Build AST of filter-call or map-call expression by grammar in test case
     *
     * @return abstract syntax tree of filter-call or map-call
     * @throws InvalidSyntaxException if cannot parse expected expression (source has syntax error)
     * @throws InvalidTypeException   if cannot match output types between operators and operands or
     *                                output type of expression does not match to the call-type
     */
    public ASTree build() throws InvalidSyntaxException, InvalidTypeException {
        if (isMatchesTo(NodeType.MAP_EXPRESSION)) {
            matchTo(NodeType.MAP_EXPRESSION);
            if (!matchTo('{'))
                throw new InvalidSyntaxException("Expected opening bracket '{'!");
            ASTNode root = getExpression();
            if (!matchTo('}'))
                throw new InvalidSyntaxException("Expected closing bracket '}'!");
            if (!end())
                throw new InvalidSyntaxException("Unexpected symbols after closing bracket '}'!");
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
                if (!end())
                    throw new InvalidSyntaxException("Unexpected symbols after closing bracket '}'!");
                if (root.outputType != OutputType.BOOLEAN)
                    throw new InvalidTypeException("Expected integer output type!");
                return new ASTree(root, elements, NodeType.FILTER_EXPRESSION);
            }
        }
        throw new InvalidSyntaxException("Cannot match to any call-expression!");
    }

    /**
     * Creates default filter-call expression tree
     *
     * @return filter-call AST "filter{(1=1)}"
     */
    public static ASTree buildDefaultFilter() {
        ASTNode first = new ASTNode("1", OutputType.INT, NodeType.NUMBER);
        ASTNode second = new ASTNode("1", OutputType.INT, NodeType.NUMBER);
        ASTNode operator = new ASTNode("=", OutputType.BOOLEAN, NodeType.OPERATION);
        operator.addChild(first);
        operator.addChild(second);
        return new ASTree(operator, NodeType.FILTER_EXPRESSION);
    }

    /**
     * Creates default map-call expression tree
     *
     * @return map-call AST "map{element}"
     */
    public static ASTree buildDefaultMap() {
        ASTNode element = new ASTNode("element", OutputType.INT, NodeType.ELEMENT);
        ArrayList<ASTNode> elList = new ArrayList<>();
        elList.add(element);
        return new ASTree(element, elList, NodeType.MAP_EXPRESSION);
    }

    /**
     * Rebuilds given map- and filter-calls to the form "filter{expression}%>%map{expression}"
     *
     * @param calls map- and filter-calls to rebuild
     * @return List of two calls AST (filter and map) that forms to "filter{expression}%>%map{expression}"
     * @throws InvalidTypeException if got not map- or filter-call AST
     */
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
