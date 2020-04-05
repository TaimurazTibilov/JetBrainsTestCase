package filtermapinterpreter;

import com.sun.jdi.InvalidTypeException;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class-interpreter, has static method that interprets given source string
 * to format "filter{expression}%>%map{expression}"
 *
 * @author Taimuraz Tibilov
 */
public class FilterMapInterpreter {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println(interpret(input.nextLine()));
    }

    /**
     * Method that interprets given source string to format
     * "filter{expression}%>%map{expression}" or returns string with
     * description of error that happened while parsed
     *
     * @param source string that contains expression built by test case grammar
     * @return expression formed as "filter{expression}%>%map{expression}" or string with
     * description of error that happened while parsed
     */
    public static String interpret(String source) {
        ArrayList<ASTree> callExpressions = new ArrayList<>();
        boolean hasFilter = false;
        boolean hasMap = false;
        String[] calls = new String[0];
        if (!source.isBlank())
            calls = source.split("%>%");
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
