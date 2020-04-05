package filtermapinterpreter;

public abstract class AbstractParser {

    public static final String WHITESPACES = " \n\r\t\f";
    public static final String OPERATORS = "+-*<>=&|";
    public static final String BRACKETS = "(){}";

    private String source = "";
    private int pos = 0;

    public AbstractParser(String source) {
        this.source = source;
    }

    public boolean end() {
        return pos >= source.length();
    }

    public void next() {
        if (!end())
            pos++;
    }

    public char current() {
        if (end())
            return 0;
        return source.charAt(pos);
    }

    public void skipSpaces() {
        while (WHITESPACES.indexOf(current()) != -1)
            next();
    }

    public boolean isMatchesTo(NodeType type) {
        if (type == null)
            return false;
        int previousPos = this.pos;
        boolean match = false;
        switch (type) {
            case NUMBER:
                match = !matchTo(NodeType.NUMBER).isEmpty();
                break;
            case ELEMENT:
                match = !matchTo(NodeType.ELEMENT).isEmpty();
                break;
            case OPERATION:
                match = !matchTo(NodeType.OPERATION).isEmpty();
                break;
            case FILTER_EXPRESSION:
                match = !matchTo(NodeType.FILTER_EXPRESSION).isEmpty();
                break;
            case MAP_EXPRESSION:
                match = !matchTo(NodeType.MAP_EXPRESSION).isEmpty();
                break;
            default: // this matcher works only with elementary or primitive types
                break;
        }
        this.pos = previousPos;
        return match;
    }

    protected String matchTo(NodeType type) {
        if (type == null)
            return "";
        skipSpaces();
        StringBuilder builder = new StringBuilder();
        switch (type) {
            case NUMBER:
                if (current() == '-') {
                    builder.append(current());
                    next();
                }
                if (Character.isDigit(current())) {
                    do {
                        builder.append(current());
                        next();
                    } while (Character.isDigit(current()));
                    return builder.toString();
                }
                break;
            case ELEMENT:
                if (source.startsWith("element", pos)) {
                    pos += 7;
                    return "element";
                }
                break;
            case OPERATION:
                if (OPERATORS.indexOf(current()) > -1)
                    return String.valueOf(source.charAt(pos++));
                break;
            case FILTER_EXPRESSION:
                if (source.startsWith("filter", pos)) {
                    pos += 6;
                    return "filter";
                }
            case MAP_EXPRESSION:
                if (source.startsWith("map", pos)) {
                    pos += 3;
                    return "map";
                }
                break;
            default: // this matcher works only with elementary or primitive types
                break;
        }
        return ""; // if no matches are found
    }

    protected boolean matchTo(char bracket) {
        if (end())
            return false;
        if (BRACKETS.indexOf(bracket) == -1)
            return false;
        return source.charAt(pos++) == bracket;
    }
}
