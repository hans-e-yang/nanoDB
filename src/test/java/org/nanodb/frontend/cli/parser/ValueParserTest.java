package org.nanodb.frontend.cli.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.internal.condition.*;
import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Pair;
import org.nanodb.cli.parser.ValueParser;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValueParser class
 * Tests parsing of string values to typed values according to schema
 */
public class ValueParserTest {
    private StructType testSchema;

    @BeforeEach
    public void setUp() {
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("id", new Int32Type()));
        fields.add(new Pair<>("name", new VarCharType(50)));
        fields.add(new Pair<>("email", new VarCharType(100)));
        fields.add(new Pair<>("age", new Int32Type()));
        fields.add(new Pair<>("birthdate", new DateType()));
        testSchema = new StructType(fields);
    }

    // ==================== parseValue() tests ====================

    @Test
    public void testParseValue_Integer_Valid() throws ParseException {
        Object result = ValueParser.parseValue("42", new Int32Type());
        assertEquals(42, result);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testParseValue_Integer_WithSpaces() throws ParseException {
        Object result = ValueParser.parseValue("  123  ", new Int32Type());
        assertEquals(123, result);
    }

    @Test
    public void testParseValue_Integer_Invalid() {
        assertThrows(ParseException.class, () -> {
            ValueParser.parseValue("not-a-number", new Int32Type());
        });
    }

    @Test
    public void testParseValue_Integer_Negative() throws ParseException {
        Object result = ValueParser.parseValue("-99", new Int32Type());
        assertEquals(-99, result);
    }

    @Test
    public void testParseValue_VarChar() throws ParseException {
        Object result = ValueParser.parseValue("Hello World", new VarCharType(50));
        assertEquals("Hello World", result);
        assertTrue(result instanceof String);
    }

    @Test
    public void testParseValue_VarChar_WithSpaces() throws ParseException {
        Object result = ValueParser.parseValue("  test  ", new VarCharType(20));
        assertEquals("test", result); // Trimmed
    }

    @Test
    public void testParseValue_Date_YearMonthDay() throws ParseException {
        Object result = ValueParser.parseValue("2025-11-13", new DateType());
        assertNotNull(result);
        assertTrue(result instanceof Date);
    }

    @Test
    public void testParseValue_Date_YearMonthDaySlash() throws ParseException {
        Object result = ValueParser.parseValue("2025/11/13", new DateType());
        assertNotNull(result);
        assertTrue(result instanceof Date);
    }

    @Test
    public void testParseValue_Date_DayMonthYear() throws ParseException {
        Object result = ValueParser.parseValue("13-11-2025", new DateType());
        assertNotNull(result);
        assertTrue(result instanceof Date);
    }

    @Test
    public void testParseValue_Date_Invalid() {
        assertThrows(ParseException.class, () -> {
            ValueParser.parseValue("not-a-date", new DateType());
        });
    }

    @Test
    public void testParseValue_Null() throws ParseException {
        Object result = ValueParser.parseValue(null, new Int32Type());
        assertNull(result);
    }

    // ==================== parseValues() tests ====================

    @Test
    public void testParseValues_Success() throws ParseException {
        Map<String, String> input = new HashMap<>();
        input.put("id", "1");
        input.put("name", "Alice");
        input.put("age", "30");

        Map<String, Object> result = ValueParser.parseValues(input, testSchema);

        assertEquals(3, result.size());
        assertEquals(1, result.get("id"));
        assertEquals("Alice", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    @Test
    public void testParseValues_MultipleTypes() throws ParseException {
        Map<String, String> input = new HashMap<>();
        input.put("id", "100");
        input.put("name", "Bob");
        input.put("email", "bob@example.com");
        input.put("age", "25");
        input.put("birthdate", "2000-01-15");

        Map<String, Object> result = ValueParser.parseValues(input, testSchema);

        assertEquals(5, result.size());
        assertTrue(result.get("id") instanceof Integer);
        assertTrue(result.get("name") instanceof String);
        assertTrue(result.get("email") instanceof String);
        assertTrue(result.get("age") instanceof Integer);
        assertTrue(result.get("birthdate") instanceof Date);
    }

    @Test
    public void testParseValues_NullSchema() {
        Map<String, String> input = new HashMap<>();
        input.put("id", "1");

        assertThrows(ParseException.class, () -> {
            ValueParser.parseValues(input, null);
        });
    }

    @Test
    public void testParseValues_ColumnNotInSchema() {
        Map<String, String> input = new HashMap<>();
        input.put("invalidColumn", "value");

        assertThrows(ParseException.class, () -> {
            ValueParser.parseValues(input, testSchema);
        });
    }

    @Test
    public void testParseValues_InvalidType() {
        Map<String, String> input = new HashMap<>();
        input.put("age", "not-a-number");

        assertThrows(ParseException.class, () -> {
            ValueParser.parseValues(input, testSchema);
        });
    }

    @Test
    public void testParseValues_EmptyMap() throws ParseException {
        Map<String, String> input = new HashMap<>();
        Map<String, Object> result = ValueParser.parseValues(input, testSchema);
        
        assertEquals(0, result.size());
    }

    // ==================== parseConditionValues() tests ====================

    @Test
    public void testParseConditionValues_SimpleComparison() throws ParseException {
        ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        
        Condition parsed = ValueParser.parseConditionValues(condition, testSchema);
        
        assertNotNull(parsed);
        assertTrue(parsed instanceof ComparisonCondition);
        ComparisonCondition parsedComp = (ComparisonCondition) parsed;
        assertEquals(18, parsedComp.getValue());
        assertTrue(parsedComp.getValue() instanceof Integer);
    }

    @Test
    public void testParseConditionValues_StringComparison() throws ParseException {
        ComparisonCondition condition = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");
        
        Condition parsed = ValueParser.parseConditionValues(condition, testSchema);
        
        assertTrue(parsed instanceof ComparisonCondition);
        ComparisonCondition parsedComp = (ComparisonCondition) parsed;
        assertEquals("Alice", parsedComp.getValue());
        assertTrue(parsedComp.getValue() instanceof String);
    }

    @Test
    public void testParseConditionValues_AndCondition() throws ParseException {
        ComparisonCondition left = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        ComparisonCondition right = new ComparisonCondition("id", ComparisonOperation.EQUAL, "5");
        AndCondition andCondition = new AndCondition(left, right);
        
        Condition parsed = ValueParser.parseConditionValues(andCondition, testSchema);
        
        assertTrue(parsed instanceof AndCondition);
        AndCondition parsedAnd = (AndCondition) parsed;
        ComparisonCondition parsedLeft = (ComparisonCondition) parsedAnd.getLeft();
        ComparisonCondition parsedRight = (ComparisonCondition) parsedAnd.getRight();
        assertEquals(18, parsedLeft.getValue());
        assertEquals(5, parsedRight.getValue());
    }

    @Test
    public void testParseConditionValues_OrCondition() throws ParseException {
        ComparisonCondition left = new ComparisonCondition("age", ComparisonOperation.LESS_THAN, "65");
        ComparisonCondition right = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Admin");
        OrCondition orCondition = new OrCondition(left, right);
        
        Condition parsed = ValueParser.parseConditionValues(orCondition, testSchema);
        
        assertTrue(parsed instanceof OrCondition);
        OrCondition parsedOr = (OrCondition) parsed;
        ComparisonCondition parsedLeft = (ComparisonCondition) parsedOr.getLeft();
        ComparisonCondition parsedRight = (ComparisonCondition) parsedOr.getRight();
        assertEquals(65, parsedLeft.getValue());
        assertEquals("Admin", parsedRight.getValue());
    }

    @Test
    public void testParseConditionValues_NestedConditions() throws ParseException {
        // (age > 18 AND id = 5) OR name = 'Alice'
        ComparisonCondition cond1 = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        ComparisonCondition cond2 = new ComparisonCondition("id", ComparisonOperation.EQUAL, "5");
        ComparisonCondition cond3 = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");
        
        AndCondition andCond = new AndCondition(cond1, cond2);
        OrCondition orCond = new OrCondition(andCond, cond3);
        
        Condition parsed = ValueParser.parseConditionValues(orCond, testSchema);
        
        assertTrue(parsed instanceof OrCondition);
        OrCondition parsedOr = (OrCondition) parsed;
        AndCondition parsedAnd = (AndCondition) parsedOr.getLeft();
        ComparisonCondition parsedCond1 = (ComparisonCondition) parsedAnd.getLeft();
        ComparisonCondition parsedCond2 = (ComparisonCondition) parsedAnd.getRight();
        ComparisonCondition parsedCond3 = (ComparisonCondition) parsedOr.getRight();
        
        assertEquals(18, parsedCond1.getValue());
        assertEquals(5, parsedCond2.getValue());
        assertEquals("Alice", parsedCond3.getValue());
    }

    @Test
    public void testParseConditionValues_NullCondition() throws ParseException {
        // Should return null
        Condition result = ValueParser.parseConditionValues(null, testSchema);
        assertNull(result);
    }

    @Test
    public void testParseConditionValues_InvalidColumn() {
        ComparisonCondition condition = new ComparisonCondition("invalidColumn", ComparisonOperation.EQUAL, "value");
        
        assertThrows(ParseException.class, () -> {
            ValueParser.parseConditionValues(condition, testSchema);
        });
    }

    @Test
    public void testParseConditionValues_InvalidType() {
        ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.EQUAL, "not-a-number");
        
        assertThrows(ParseException.class, () -> {
            ValueParser.parseConditionValues(condition, testSchema);
        });
    }

    @Test
    public void testParseConditionValues_DateComparison() throws ParseException {
        ComparisonCondition condition = new ComparisonCondition("birthdate", ComparisonOperation.GREATER_THAN, "2000-01-01");
        
        Condition parsed = ValueParser.parseConditionValues(condition, testSchema);
        
        assertTrue(parsed instanceof ComparisonCondition);
        ComparisonCondition parsedComp = (ComparisonCondition) parsed;
        assertTrue(parsedComp.getValue() instanceof Date);
    }

    @Test
    public void testParseConditionValues_ComplexNesting() throws ParseException {
        // ((age > 18 AND age < 65) AND name = 'Alice') OR id = 999
        ComparisonCondition ageLower = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        ComparisonCondition ageUpper = new ComparisonCondition("age", ComparisonOperation.LESS_THAN, "65");
        ComparisonCondition nameCheck = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");
        ComparisonCondition idCheck = new ComparisonCondition("id", ComparisonOperation.EQUAL, "999");
        
        AndCondition ageRange = new AndCondition(ageLower, ageUpper);
        AndCondition ageAndName = new AndCondition(ageRange, nameCheck);
        OrCondition finalCond = new OrCondition(ageAndName, idCheck);
        
        Condition parsed = ValueParser.parseConditionValues(finalCond, testSchema);
        
        assertTrue(parsed instanceof OrCondition);
        OrCondition parsedOr = (OrCondition) parsed;
        AndCondition parsedAgeAndName = (AndCondition) parsedOr.getLeft();
        AndCondition parsedAgeRange = (AndCondition) parsedAgeAndName.getLeft();
        ComparisonCondition parsedAgeLower = (ComparisonCondition) parsedAgeRange.getLeft();
        ComparisonCondition parsedAgeUpper = (ComparisonCondition) parsedAgeRange.getRight();
        ComparisonCondition parsedNameCheck = (ComparisonCondition) parsedAgeAndName.getRight();
        ComparisonCondition parsedIdCheck = (ComparisonCondition) parsedOr.getRight();
        
        assertEquals(18, parsedAgeLower.getValue());
        assertEquals(65, parsedAgeUpper.getValue());
        assertEquals("Alice", parsedNameCheck.getValue());
        assertEquals(999, parsedIdCheck.getValue());
    }
}
