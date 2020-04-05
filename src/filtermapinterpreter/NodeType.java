package filtermapinterpreter;

/**
 * Represents types of nodes of abstract syntax tree
 *
 * @author Taimuraz Tibilov
 */
public enum NodeType {
    UNKNOWN, // isn't used in this case
    NUMBER, // [0-9] | -[0-9]
    ELEMENT, // "element"
    OPERATION, // "*" | "+" | "-" | "<" | ">" | "=" | "&" | "|"
    EXPRESSION, // ELEMENT | NUMBER | "("EXPRESSION OPERATION EXPRESSION")" (isn't used in this case)
    FILTER_EXPRESSION, // "filter{" EXPRESSION "}"
    MAP_EXPRESSION, // "map{" EXPRESSION "}"
    NIL // NIL-Object
}
