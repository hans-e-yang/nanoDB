package org.nanodb.internal.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComparisonCondition class
 * Tests all getter methods, constructor, and accept method
 */
public class ComparisonConditionTest {
    private ComparisonCondition condition;

    @BeforeEach
    public void setUp() {
        condition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "5");
    }

    @Test
    public void testConstructor_WithStringValue() {
        ComparisonCondition newCondition = new ComparisonCondition("name", ComparisonOperation.LIKE, "John%");
        assertNotNull(newCondition);
        assertEquals("name", newCondition.getColumn());
        assertEquals(ComparisonOperation.LIKE, newCondition.getOp());
        assertEquals("John%", newCondition.getValue());
    }

    @Test
    public void testGetColumn() {
        assertEquals("id", condition.getColumn());
    }

    @Test
    public void testGetOp_Equal() {
        assertEquals(ComparisonOperation.EQUAL, condition.getOp());
    }

    @Test
    public void testGetOp_GreaterThan() {
        ComparisonCondition gtCondition = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        assertEquals(ComparisonOperation.GREATER_THAN, gtCondition.getOp());
    }

    @Test
    public void testGetOp_LessThan() {
        ComparisonCondition ltCondition = new ComparisonCondition("price", ComparisonOperation.LESS_THAN, "100");
        assertEquals(ComparisonOperation.LESS_THAN, ltCondition.getOp());
    }

    @Test
    public void testGetOp_GreaterEquals() {
        ComparisonCondition geCondition = new ComparisonCondition("score", ComparisonOperation.GREATER_EQUALS, "75");
        assertEquals(ComparisonOperation.GREATER_EQUALS, geCondition.getOp());
    }

    @Test
    public void testGetOp_LessEquals() {
        ComparisonCondition leCondition = new ComparisonCondition("rating", ComparisonOperation.LESS_EQUALS, "5");
        assertEquals(ComparisonOperation.LESS_EQUALS, leCondition.getOp());
    }

    @Test
    public void testGetOp_NotEquals() {
        ComparisonCondition neCondition = new ComparisonCondition("status", ComparisonOperation.NOT_EQUALS, "inactive");
        assertEquals(ComparisonOperation.NOT_EQUALS, neCondition.getOp());
    }

    @Test
    public void testGetOp_Like() {
        ComparisonCondition likeCondition = new ComparisonCondition("email", ComparisonOperation.LIKE, "%@example.com");
        assertEquals(ComparisonOperation.LIKE, likeCondition.getOp());
    }

    @Test
    public void testGetValue() {
        assertEquals("5", condition.getValue());
    }

    @Test
    public void testAccept_CallsVisitorCorrectly() {
        // Create a mock visitor
        MockConditionVisitor mockVisitor = new MockConditionVisitor(true);
        boolean result = condition.accept(mockVisitor);
        
        assertTrue(result);
        assertTrue(mockVisitor.wasVisitComparisonCalled);
        assertEquals(condition, mockVisitor.lastVisitedCondition);
    }

    @Test
    public void testAccept_ReturnsFalse() {
        MockConditionVisitor mockVisitor = new MockConditionVisitor(false);
        boolean result = condition.accept(mockVisitor);
        
        assertFalse(result);
        assertTrue(mockVisitor.wasVisitComparisonCalled);
    }

    @Test
    public void testGetStringValue_WithParsedNonStringValue() {
        ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.EQUAL, "25");
        // Parse the string value to an Integer
        condition.setParsedValue(Integer.valueOf(25));
        
        // getStringValue should call toString() on the non-String object
        assertEquals("25", condition.getStringValue());
        assertEquals(25, condition.getValue());
    }

    // Mock visitor for testing
    private static class MockConditionVisitor implements ConditionVisitor {
        private final boolean returnValue;
        boolean wasVisitComparisonCalled = false;
        ComparisonCondition lastVisitedCondition = null;

        public MockConditionVisitor(boolean returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public boolean visitComparison(ComparisonCondition comparisonCondition) {
            wasVisitComparisonCalled = true;
            lastVisitedCondition = comparisonCondition;
            return returnValue;
        }
    }
}
