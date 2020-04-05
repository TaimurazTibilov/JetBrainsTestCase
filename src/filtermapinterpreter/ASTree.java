package filtermapinterpreter;

import com.sun.jdi.InvalidTypeException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class of abstract syntax tree (AST) with root of type {@code ASTNode},
 * represents AST of filter- or map-call expression. Also has merging and combining method.
 *
 * @author Taimuraz Tibiblov
 */
public class ASTree {

    private ASTNode root; // Root node, represent expression
    private List<ASTNode> elementNodes; // List of element of the tree (unchecked), needed for fast merging
    private NodeType rootType; // MAP_EXPRESSION or FILTER_EXPRESSION

    /**
     * Constructor, throws {@code InvalidParameterException} if there are problems with matching of types
     *
     * @param root         Root node, represent expression
     * @param elementNodes List of element of the tree (unchecked), needed for fast merging
     * @param rootType     MAP_EXPRESSION or FILTER_EXPRESSION
     */
    public ASTree(ASTNode root, List<ASTNode> elementNodes, NodeType rootType) {
        if (root == null || root == ASTNode.NIL || elementNodes == null || rootType == null)
            throw new InvalidParameterException("Parameters cannot be a null!");
        if (root.outputType == OutputType.INT) {
            if (rootType != NodeType.MAP_EXPRESSION)
                throw new InvalidParameterException("Cannot map by boolean value!");
        } else {
            if (root.outputType == OutputType.BOOLEAN) {
                if (rootType != NodeType.FILTER_EXPRESSION)
                    throw new InvalidParameterException("Cannot filter by integer value!");
            } else
                throw new InvalidParameterException("Wrong output type of root!");
        }
        this.root = root;
        this.elementNodes = elementNodes;
        this.rootType = rootType;
    }

    /**
     * Constructor, throws {@code InvalidParameterException} if there are problems with matching of types.
     * Creates list of ELEMENT nodes.
     *
     * @param root     Root node, represent expression
     * @param rootType MAP_EXPRESSION or FILTER_EXPRESSION
     */
    public ASTree(ASTNode root, NodeType rootType) {
        this(root, new ArrayList<ASTNode>(), rootType);
    }

    /**
     * Override method. Build description of call-expression by grammar rules of the case
     *
     * @return string formatted like "filter{expression}" or "map{expression}"
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (rootType == NodeType.MAP_EXPRESSION)
            builder.append("map{");
        else
            builder.append("filter{");
        builder.append(root.toString());
        builder.append("}");
        return builder.toString();
    }

    /**
     * Getter of the root type
     *
     * @return root type
     */
    public NodeType getRootType() {
        return rootType;
    }

    /**
     * Helper method, rebuilds structure by the following rules:
     * 1. If this tree is a filter tree - throw an invalid type exception
     * 2. If this tree is a map, then replace all ELEMENT nodes of the given
     * tree by this tree root expression.
     *
     * @param newRoot tree that this merges to
     */
    public void mergeTo(ASTree newRoot) throws InvalidTypeException {
        if (newRoot == null)
            throw new InvalidParameterException("Null pointer on new tree!");
        if (rootType != NodeType.MAP_EXPRESSION)
            throw new InvalidTypeException("Can merge only map to tree!");
        ASTNode.replaceAll(newRoot.elementNodes, root);
    }

    /**
     * Combines filter-calls to new one by rule:
     * "filter{exp1} %>% filter{exp2}  ->  filter{(exp1 & exp2)}"
     * If one of calls is {@code null}, returns another one (not-null)
     *
     * @param filterTree1 first filter-call tree
     * @param filterTree2 second filter-call tree
     * @return filter-call combination of the given filters
     */
    public static ASTree combine(ASTree filterTree1, ASTree filterTree2) {
        if (filterTree1 == null) {
            if (filterTree2 == null)
                throw new InvalidParameterException("Null pointer on parameters!");
            if (filterTree2.rootType == NodeType.FILTER_EXPRESSION)
                return filterTree2;
            else
                throw new InvalidParameterException("Trees are not filter expression!");
        }
        if (filterTree2 == null) {
            if (filterTree1.rootType == NodeType.FILTER_EXPRESSION)
                return filterTree1;
        }
        if (filterTree1.rootType != NodeType.FILTER_EXPRESSION || filterTree2.rootType != NodeType.FILTER_EXPRESSION)
            throw new InvalidParameterException("Trees are not filter expression!");
        ASTNode newRoot = new ASTNode("&", OutputType.BOOLEAN, NodeType.OPERATION);
        newRoot.addChild(filterTree1.root);
        newRoot.addChild(filterTree2.root);
        List<ASTNode> elements = new ArrayList<>(filterTree1.elementNodes);
        elements.addAll(filterTree2.elementNodes);
        return new ASTree(newRoot, elements, NodeType.FILTER_EXPRESSION);
    }
}
