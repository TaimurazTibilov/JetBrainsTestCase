package filtermapinterpreter;

/**
 * Abstract class for parsing call-expressions and their parts.
 * It has methods to match or check matching for each type of expression member.
 *
 * @author Taimuraz Tibilov
 */
public abstract class AbstractParser {

    public static final String OPERATORS = "+-*<>=&|"; // Binary operators
    public static final String BRACKETS = "(){}"; // Brackets of expressions

    private String source = "\0"; // String to parse
    private int pos = 0; // Pointer on current position

    /**
     * Constructor
     *
     * @param source call expression to parse. If got null or empty - do nothing.
     */
    public AbstractParser(String source) {
        if (source != null && source.length() > 0)
            this.source = source;
    }

    /**
     * Shows is pointer out of bound
     *
     * @return is pointer reached end of source
     */
    public boolean end() {
        return pos >= source.length();
    }

    /**
     * Increment position pointer
     */
    public void next() {
        if (!end())
            pos++;
    }

    /**
     * Returns current character of source string or null-char if end
     *
     * @return current character of source string or null-char if end
     */
    public char current() {
        if (end())
            return '\0';
        return source.charAt(pos);
    }

    /**
     * Method skips whitespaces (isn't used in this realization)
     */
    public void skipSpaces() {
        while (!end() && Character.isWhitespace(current()))
            next();
    }

    /**
     * Shows, does current prefix of the source string from position matches to given type of expression
     *
     * @param type grammatical type of expression to match to
     * @return true if prefix of the source matches to the given type, false otherwise
     */
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

    /**
     * Try to match prefix of the source from current position to given type.
     *
     * @param type grammatical type of expression to match to
     * @return value if prefix of the source matches to the given type, empty string otherwise
     */
    protected String matchTo(NodeType type) {
        if (type == null)
            return "";
//        skipSpaces(); // Uses if needed
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
                    } while (!end() && Character.isDigit(current()));
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

    /**
     * Override of method {@code matchTo(NodeType type)}. Needed to match to the brackets
     *
     * @param bracket bracket that expected in source
     * @return true if bracket matches to expected bracket, false otherwise
     */
    protected boolean matchTo(char bracket) {
//        skipSpaces(); // Uses if needed
        if (end())
            return false;
        if (BRACKETS.indexOf(bracket) == -1)
            return false;
        return source.charAt(pos++) == bracket;
    }
}
