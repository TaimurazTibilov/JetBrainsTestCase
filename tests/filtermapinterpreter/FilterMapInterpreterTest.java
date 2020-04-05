package filtermapinterpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterMapInterpreterTest {

    @Test
    void interpretTest() {
        // Empty or blank source
        String actual = FilterMapInterpreter.interpret("");
        assertEquals("filter{(1=1)}%>%map{element}", actual);

        actual = FilterMapInterpreter.interpret(" \t");
        assertEquals("filter{(1=1)}%>%map{element}", actual);

        // Adding filter
        actual = FilterMapInterpreter.interpret("map{-1}");
        assertEquals("filter{(1=1)}%>%map{-1}", actual);

        // Adding map
        actual = FilterMapInterpreter.interpret("filter{(element>2)}");
        assertEquals("filter{(element>2)}%>%map{element}", actual);

        // Combine filters
        actual = FilterMapInterpreter.interpret("filter{(element>2)}%>%filter{(element<4)}%>%filter{(3<4)}");
        assertEquals("filter{(((element>2)&(element<4))&(3<4))}%>%map{element}", actual);

        // Merge map to map
        actual = FilterMapInterpreter.interpret("map{(element+4)}%>%map{(element*element)}%>%map{(element+9)}");
        assertEquals("filter{(1=1)}%>%map{(((element+4)*(element+4))+9)}", actual);

        // Merge map to filter
        actual = FilterMapInterpreter.interpret("map{(element+4)}%>%map{(element*4)}%>%filter{(element>2)}");
        assertEquals("filter{(((element+4)*4)>2)}%>%map{((element+4)*4)}", actual);
    }

    @Test
    void syntaxErrorTest() {
        // Incorrect call-chain
        String actual = FilterMapInterpreter.interpret("filter{(1=1)}>map{element}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(1=1)}%>map{element}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        // Wrong brackets
        actual = FilterMapInterpreter.interpret("map(element)");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("map{element)");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("map{element");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{element>2}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        // Whitespaces (only if there are no skipping)
        actual = FilterMapInterpreter.interpret("filter {(element>2)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{( element>2)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(element >2)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(element>2) }");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(element>2)} ");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        // Unknown tokens
        actual = FilterMapInterpreter.interpret("filt{(1=1)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("mapping{1}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("map{elem}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("map{(1/2)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        // Missing argument
        actual = FilterMapInterpreter.interpret("map{(1*)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(<element)}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));

        actual = FilterMapInterpreter.interpret("filter{}");
        assertTrue(actual.startsWith("SYNTAX ERROR"));
    }

    @Test
    void typeErrorTest() {
        // Wrong type of call output
        String actual = FilterMapInterpreter.interpret("map{(1>2)}");
        assertTrue(actual.startsWith("TYPE ERROR"));

        actual = FilterMapInterpreter.interpret("filter{element}");
        assertTrue(actual.startsWith("TYPE ERROR"));

        actual = FilterMapInterpreter.interpret("filter{(1+2)}");
        assertTrue(actual.startsWith("TYPE ERROR"));

        // Wrong type of operand
        actual = FilterMapInterpreter.interpret("map{(element>(1=1))}");
        assertTrue(actual.startsWith("TYPE ERROR"));

        actual = FilterMapInterpreter.interpret("map{(element&(1=1))}");
        assertTrue(actual.startsWith("TYPE ERROR"));

        actual = FilterMapInterpreter.interpret("map{(element+(1=1))}");
        assertTrue(actual.startsWith("TYPE ERROR"));
    }
}