package filtermapinterpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents node of abstract syntax tree (AST).
 *
 * @author Taimuraz Tibilov
 */
public class ASTNode {

    public final String value; // String value, needed for toString() method
    public final OutputType outputType; // Output type of node expression
    public final NodeType type; // Type of the node (e.g. ELEMENT or OPERATION)
    public static final ASTNode NIL = new ASTNode("nil", OutputType.NONE, NodeType.NIL); // Used instead of null

    private ASTNode parent; // Parent node
    private ArrayList<ASTNode> children; // List of children nodes

    /**
     * Constructor
     *
     * @param value       String value, needed for toString() method
     * @param output_type Output type of node expression
     * @param type        Type of the node (e.g. ELEMENT or OPERATION)
     */
    public ASTNode(String value, OutputType output_type, NodeType type) {
        this.value = value;
        outputType = output_type;
        this.type = type;
        parent = NIL;
        children = new ArrayList<>();
    }

    /**
     * Replaces old node by new one in parent node (types are unchecked)
     *
     * @param oldNode old node of AST
     * @param newNode new node of AST
     */
    public static void replace(ASTNode oldNode, ASTNode newNode) {
        if (oldNode == null || oldNode == NIL || newNode == null || newNode == NIL)
            return;
        ASTNode parent = oldNode.parent;
        oldNode.parent = NIL;
        if (parent == NIL)
            return;
        parent.children.replaceAll(x -> x == oldNode ? newNode : x);
    }

    /**
     * Replaces all old nodes by new one in their parent node (types are unchecked)
     *
     * @param oldNodes old nodes of AST
     * @param newNode  new node of AST
     */
    public static void replaceAll(List<ASTNode> oldNodes, ASTNode newNode) {
        if (newNode == null || newNode == NIL || oldNodes == null)
            return;
        for (ASTNode oldNode : oldNodes) {
            replace(oldNode, newNode);
        }
    }

    /**
     * Override method. Builds description formed as "(statement operator statement)" or "statement"
     *
     * @return description of node by case grammar rules
     */
    @Override
    public String toString() {
        if (type != NodeType.OPERATION)
            return value;
        if (childrenCount() != 2)
            return "UNKNOWN ERROR - there are not 2 operands in binary operation!";
        return "(" +
                children.get(0) +
                value +
                children.get(1) +
                ")";
    }

    /**
     * Adds a child to children list. Do nothing if null or NIL
     *
     * @param child node of this node to add to the children
     */
    public void addChild(ASTNode child) {
        if (child == null || child == NIL || type == NodeType.NIL)
            return;
        child.parent.removeChild(child);
        child.parent = this;
        children.add(child);
    }

    /**
     * Removes given child from list of children. Do nothing if null
     * or NIL or not exists in list
     *
     * @param child node to remove from a list of children
     */
    public void removeChild(ASTNode child) {
        if (child == null || child == NIL)
            return;
        children.remove(child);
        if (child.parent == this) {
            child.parent = NIL;
        }
    }

    /**
     * Getter of children number
     *
     * @return number of children
     */
    public int childrenCount() {
        return children.size();
    }
}
