package filtermapinterpreter;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {

    public final String value;
    public final OutputType outputType;
    public final NodeType type;
    public static final ASTNode NIL = new ASTNode("nil", OutputType.NONE, NodeType.NIL);

    private ASTNode parent;
    private ArrayList<ASTNode> children;

    public ASTNode(String value, OutputType output_type, NodeType type) {
        this.value = value;
        outputType = output_type;
        this.type = type;
        parent = NIL;
        children = new ArrayList<>();
    }

    public static void replace(ASTNode oldNode, ASTNode newNode) {
        if (oldNode == null || oldNode == NIL || newNode == null || newNode == NIL)
            return;
        ASTNode parent = oldNode.parent;
        oldNode.parent = NIL;
        if (parent == NIL)
            return;
        parent.children.replaceAll(x -> x == oldNode ? newNode : x);
    }

    public static void replaceAll(List<ASTNode> oldNodes, ASTNode newNode) {
        if (newNode == null || newNode == NIL || oldNodes == null)
            return;
        for (ASTNode oldNode : oldNodes) {
            replace(oldNode, newNode);
        }
    }

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

    public void addChild(ASTNode child) {
        if (child == null || child == NIL)
            return;
        child.parent.removeChild(child);
        child.parent = this;
        children.add(child);
    }

    public void removeChild(ASTNode child) {
        if (child == null || child == NIL)
            return;
        children.remove(child);
        if (child.parent == this) {
            child.parent = NIL;
        }
    }

    public ASTNode getChild(int index) {
        try {
            return children.get(index);
        } catch (IndexOutOfBoundsException e) {
            return NIL;
        }
    }

    public ArrayList<ASTNode> getChildren() {
        return (ArrayList<ASTNode>) children.clone();
    }

    public int childrenCount() {
        return children.size();
    }
}
