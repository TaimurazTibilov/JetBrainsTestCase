package filtermapinterpreter;

import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.Scanner;

public class FilterMapInterpreter {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println(interpret(input.nextLine()));
    }

    public static String interpret(String source) {
        ArrayList<ASTree> callExpressions = new ArrayList<>();
        boolean hasFilter = false;
        boolean hasMap = false;
        String[] calls = source.split("%>%");
        int counter = 0;
        for (String call : calls) {
            counter++;
            ASTreeBuilder builder = new ASTreeBuilder(call);
            try {
                ASTree tree = builder.build();
                if (tree.getRootType() == NodeType.FILTER_EXPRESSION)
                    hasFilter = true;
                if (tree.getRootType() == NodeType.MAP_EXPRESSION)
                    hasMap = true;
                callExpressions.add(tree);
            } catch (InvalidSyntaxException e) {
                return String.format("SYNTAX ERROR in %d block: %s", counter, e.getMessage());
            } catch (InvalidTypeException e) {
                return String.format("TYPE ERROR in %d block: %s", counter, e.getMessage());
            }
        }
        if (!hasFilter) {
            callExpressions.add(ASTreeBuilder.buildDefaultFilter());
        }
        if (!hasMap) {
            callExpressions.add(ASTreeBuilder.buildDefaultMap());
        }
        try {
            callExpressions = ASTreeBuilder.rebuildToFilterMap(callExpressions);
        } catch (InvalidTypeException e) {
            return String.format("TYPE ERROR: cannot rebuild calls, %s", e.getMessage());
        }
        return callExpressions.get(0).toString() + "%>%" + callExpressions.get(1).toString();
    }

}
