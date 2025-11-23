package org.nanodb.internal.condition.visitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValueVisitorTest {

    private Map<String, Object> row;
    private ValueVisitor visitor;

    @BeforeEach
    public void setUp() {
        row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "Alice");
        row.put("age", 30);
        row.put("score", 85.5);

        Map<String, Object> address = new HashMap<>();
        address.put("city", "New York");
        address.put("zip", 10001);
        row.put("address", address);

        List<String> tags = new ArrayList<>();
        tags.add("developer");
        tags.add("admin");
        row.put("tags", tags);
        
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(100);
        mixedList.add("test");
        row.put("mixed", mixedList);

        visitor = new ValueVisitor(row);
    }

    private ComparisonCondition createCondition(String column, ComparisonOperation op, Object value) {
        ComparisonCondition cond = new ComparisonCondition(column, op, value.toString());
        cond.setParsedValue(value);
        return cond;
    }

    @Test
    public void testVisitComparison_Equal() {
        ComparisonCondition cond = createCondition("id", ComparisonOperation.EQUAL, 1);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("id", ComparisonOperation.EQUAL, 2);
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_NotEqual() {
        ComparisonCondition cond = createCondition("name", ComparisonOperation.NOT_EQUALS, "Bob");
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("name", ComparisonOperation.NOT_EQUALS, "Alice");
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_GreaterThan() {
        ComparisonCondition cond = createCondition("age", ComparisonOperation.GREATER_THAN, 20);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("age", ComparisonOperation.GREATER_THAN, 30);
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_LessThan() {
        ComparisonCondition cond = createCondition("score", ComparisonOperation.LESS_THAN, 90.0);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("score", ComparisonOperation.LESS_THAN, 80.0);
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_GreaterEquals() {
        ComparisonCondition cond = createCondition("age", ComparisonOperation.GREATER_EQUALS, 30);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("age", ComparisonOperation.GREATER_EQUALS, 31);
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_LessEquals() {
        ComparisonCondition cond = createCondition("age", ComparisonOperation.LESS_EQUALS, 30);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("age", ComparisonOperation.LESS_EQUALS, 29);
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testVisitComparison_Like() {
        ComparisonCondition cond = createCondition("name", ComparisonOperation.LIKE, "Ali");
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("name", ComparisonOperation.LIKE, "Bob");
        assertFalse(visitor.visitComparison(cond));
    }

    @Test
    public void testNestedPath() {
        ComparisonCondition cond = createCondition("address.city", ComparisonOperation.EQUAL, "New York");
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("address.zip", ComparisonOperation.EQUAL, 10001);
        assertTrue(visitor.visitComparison(cond));
    }

    @Test
    public void testListAccess() {
        ComparisonCondition cond = createCondition("tags.0", ComparisonOperation.EQUAL, "developer");
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("tags.1", ComparisonOperation.EQUAL, "admin");
        assertTrue(visitor.visitComparison(cond));
    }

    @Test
    public void testMixedListAccess() {
        ComparisonCondition cond = createCondition("mixed.0", ComparisonOperation.EQUAL, 100);
        assertTrue(visitor.visitComparison(cond));

        cond = createCondition("mixed.1", ComparisonOperation.EQUAL, "test");
        assertTrue(visitor.visitComparison(cond));
    }

    @Test
    public void testInvalidColumn() {
        ComparisonCondition cond = createCondition("nonexistent", ComparisonOperation.EQUAL, 1);
        assertThrows(InvalidColumnNameException.class, () -> visitor.visitComparison(cond));
    }

    @Test
    public void testInvalidNestedColumn() {
        ComparisonCondition cond = createCondition("address.nonexistent", ComparisonOperation.EQUAL, 1);
        assertThrows(InvalidColumnNameException.class, () -> visitor.visitComparison(cond));
    }

    @Test
    public void testInvalidListIndex() {
        ComparisonCondition cond = createCondition("tags.5", ComparisonOperation.EQUAL, "developer");
        assertThrows(InvalidColumnNameException.class, () -> visitor.visitComparison(cond));
    }

    @Test
    public void testInvalidListIndexFormat() {
        ComparisonCondition cond = createCondition("tags.abc", ComparisonOperation.EQUAL, "developer");
        assertThrows(InvalidColumnNameException.class, () -> visitor.visitComparison(cond));
    }
}
