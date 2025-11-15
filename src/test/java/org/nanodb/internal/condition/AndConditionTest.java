package org.nanodb.internal.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AndCondition class
 * Tests the AND logical operation between two conditions
 * Coverage: Truth table for AND (T&T=T, T&F=F, F&T=F, F&F=F)
 */
public class AndConditionTest {
    private MockConditionVisitor mockVisitor;

    @BeforeEach
    public void setUp() {
        mockVisitor = new MockConditionVisitor();
    }

    @Test
    public void testConstructor() {
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        ComparisonCondition right = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");
        
        AndCondition andCondition = new AndCondition(left, right);
        assertNotNull(andCondition);
    }

    @Test
    public void testAccept_BothTrue_ReturnsTrue() {
        // left = true, right = true => result = true
        mockVisitor.setResultSequence(true, true);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.GREATER_THAN, "0");
        ComparisonCondition right = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        AndCondition andCondition = new AndCondition(left, right);
        
        boolean result = andCondition.accept(mockVisitor);
        assertTrue(result, "true AND true should return true");
        assertEquals(2, mockVisitor.visitCount);
    }

    @Test
    public void testAccept_LeftTrueRightFalse_ReturnsFalse() {
        // left = true, right = false => result = false
        mockVisitor.setResultSequence(true, false);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        ComparisonCondition right = new ComparisonCondition("status", ComparisonOperation.EQUAL, "inactive");
        AndCondition andCondition = new AndCondition(left, right);
        
        boolean result = andCondition.accept(mockVisitor);
        assertFalse(result, "true AND false should return false");
        assertEquals(2, mockVisitor.visitCount);
    }

    @Test
    public void testAccept_LeftFalseRightTrue_ReturnsFalse() {
        // left = false, right = true => result = false
        mockVisitor.setResultSequence(false, true);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "999");
        ComparisonCondition right = new ComparisonCondition("active", ComparisonOperation.EQUAL, "true");
        AndCondition andCondition = new AndCondition(left, right);
        
        boolean result = andCondition.accept(mockVisitor);
        assertFalse(result, "false AND true should return false");
        // Note: Due to short-circuit evaluation, right might not be evaluated
        // but our simple implementation evaluates both
        assertTrue(mockVisitor.visitCount >= 1);
    }

    @Test
    public void testAccept_BothFalse_ReturnsFalse() {
        // left = false, right = false => result = false
        mockVisitor.setResultSequence(false, false);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "999");
        ComparisonCondition right = new ComparisonCondition("status", ComparisonOperation.EQUAL, "deleted");
        AndCondition andCondition = new AndCondition(left, right);
        
        boolean result = andCondition.accept(mockVisitor);
        assertFalse(result, "false AND false should return false");
        assertTrue(mockVisitor.visitCount >= 1);
    }

    @Test
    public void testAccept_NestedAndConditions() {
        // Test: (cond1 AND cond2) AND cond3
        mockVisitor.setResultSequence(true, true, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("id", ComparisonOperation.GREATER_THAN, "0");
        ComparisonCondition cond2 = new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, "18");
        ComparisonCondition cond3 = new ComparisonCondition("status", ComparisonOperation.EQUAL, "active");
        
        AndCondition innerAnd = new AndCondition(cond1, cond2);
        AndCondition outerAnd = new AndCondition(innerAnd, cond3);
        
        boolean result = outerAnd.accept(mockVisitor);
        assertTrue(result, "(true AND true) AND true should return true");
    }

    @Test
    public void testAccept_NestedAndConditions_WithFalse() {
        // Test: (cond1 AND cond2) AND cond3 where cond2 is false
        mockVisitor.setResultSequence(true, false, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("id", ComparisonOperation.GREATER_THAN, "0");
        ComparisonCondition cond2 = new ComparisonCondition("age", ComparisonOperation.LESS_THAN, "0");
        ComparisonCondition cond3 = new ComparisonCondition("status", ComparisonOperation.EQUAL, "active");
        
        AndCondition innerAnd = new AndCondition(cond1, cond2);
        AndCondition outerAnd = new AndCondition(innerAnd, cond3);
        
        boolean result = outerAnd.accept(mockVisitor);
        assertFalse(result, "(true AND false) AND true should return false");
    }

    @Test
    public void testAccept_ComplexNesting() {
        // Test: cond1 AND (cond2 AND cond3)
        mockVisitor.setResultSequence(true, true, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("score", ComparisonOperation.GREATER_EQUALS, "50");
        ComparisonCondition cond2 = new ComparisonCondition("attendance", ComparisonOperation.GREATER_EQUALS, "75");
        ComparisonCondition cond3 = new ComparisonCondition("submitted", ComparisonOperation.EQUAL, "true");
        
        AndCondition innerAnd = new AndCondition(cond2, cond3);
        AndCondition outerAnd = new AndCondition(cond1, innerAnd);
        
        boolean result = outerAnd.accept(mockVisitor);
        assertTrue(result, "true AND (true AND true) should return true");
    }

    @Test
    public void testAccept_ThreeConditionsChained() {
        // Test: (cond1 AND cond2) AND cond3 - all true
        mockVisitor.setResultSequence(true, true, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("field1", ComparisonOperation.EQUAL, "value1");
        ComparisonCondition cond2 = new ComparisonCondition("field2", ComparisonOperation.EQUAL, "value2");
        ComparisonCondition cond3 = new ComparisonCondition("field3", ComparisonOperation.EQUAL, "value3");
        
        AndCondition and1 = new AndCondition(cond1, cond2);
        AndCondition and2 = new AndCondition(and1, cond3);
        
        boolean result = and2.accept(mockVisitor);
        assertTrue(result);
    }

    @Test
    public void testAccept_ThreeConditionsChained_OneFalse() {
        // Test: (cond1 AND cond2) AND cond3 - last one false
        mockVisitor.setResultSequence(true, true, false);
        
        ComparisonCondition cond1 = new ComparisonCondition("field1", ComparisonOperation.EQUAL, "value1");
        ComparisonCondition cond2 = new ComparisonCondition("field2", ComparisonOperation.EQUAL, "value2");
        ComparisonCondition cond3 = new ComparisonCondition("field3", ComparisonOperation.EQUAL, "wrongValue");
        
        AndCondition and1 = new AndCondition(cond1, cond2);
        AndCondition and2 = new AndCondition(and1, cond3);
        
        boolean result = and2.accept(mockVisitor);
        assertFalse(result);
    }

    // Mock visitor that returns a sequence of boolean values
    private static class MockConditionVisitor implements ConditionVisitor {
        private boolean[] results;
        private int visitCount = 0;

        public void setResultSequence(boolean... results) {
            this.results = results;
            this.visitCount = 0;
        }

        @Override
        public boolean visitComparison(ComparisonCondition comparisonCondition) {
            if (results != null && visitCount < results.length) {
                return results[visitCount++];
            }
            visitCount++;
            return true;
        }
    }
}
