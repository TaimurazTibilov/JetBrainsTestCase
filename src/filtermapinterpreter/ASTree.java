package filtermapinterpreter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ASTree {

    private ASTNode root;
    private List<ASTNode> elementNodes;
    private NodeType rootType;

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

    public ASTree(ASTNode root, NodeType rootType) {
        this(root, new ArrayList<ASTNode>(), rootType);
    }

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

    public NodeType getRootType() {
        return rootType;
    }

    public void mergeTo(ASTree newRoot) {
        if (newRoot == null)
            throw new InvalidParameterException("Null pointer on new tree!");
        if (rootType != NodeType.MAP_EXPRESSION)
            throw new InvalidParameterException("Can merge only map to tree!");
        ASTNode.replaceAll(newRoot.elementNodes, root);
    }

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
