package org.nanodb.internal.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrCondition class
 * Tests the OR logical operation between two conditions
 * Coverage: Truth table for OR (T|T=T, T|F=T, F|T=T, F|F=F)
 */
public class OrConditionTest {
    private MockConditionVisitor mockVisitor;

    @BeforeEach
    public void setUp() {
        mockVisitor = new MockConditionVisitor();
    }

    @Test
    public void testConstructor() {
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        ComparisonCondition right = new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");
        
        OrCondition orCondition = new OrCondition(left, right);
        assertNotNull(orCondition);
    }

    @Test
    public void testAccept_BothTrue_ReturnsTrue() {
        // left = true, right = true => result = true
        mockVisitor.setResultSequence(true, true);
        
        ComparisonCondition left = new ComparisonCondition("status", ComparisonOperation.EQUAL, "active");
        ComparisonCondition right = new ComparisonCondition("status", ComparisonOperation.EQUAL, "pending");
        OrCondition orCondition = new OrCondition(left, right);
        
        boolean result = orCondition.accept(mockVisitor);
        assertTrue(result, "true OR true should return true");
    }

    @Test
    public void testAccept_LeftTrueRightFalse_ReturnsTrue() {
        // left = true, right = false => result = true
        mockVisitor.setResultSequence(true, false);
        
        ComparisonCondition left = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, "18");
        ComparisonCondition right = new ComparisonCondition("hasPermission", ComparisonOperation.EQUAL, "true");
        OrCondition orCondition = new OrCondition(left, right);
        
        boolean result = orCondition.accept(mockVisitor);
        assertTrue(result, "true OR false should return true");
    }

    @Test
    public void testAccept_LeftFalseRightTrue_ReturnsTrue() {
        // left = false, right = true => result = true
        mockVisitor.setResultSequence(false, true);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "999");
        ComparisonCondition right = new ComparisonCondition("isAdmin", ComparisonOperation.EQUAL, "true");
        OrCondition orCondition = new OrCondition(left, right);
        
        boolean result = orCondition.accept(mockVisitor);
        assertTrue(result, "false OR true should return true");
    }

    @Test
    public void testAccept_BothFalse_ReturnsFalse() {
        // left = false, right = false => result = false
        mockVisitor.setResultSequence(false, false);
        
        ComparisonCondition left = new ComparisonCondition("id", ComparisonOperation.EQUAL, "999");
        ComparisonCondition right = new ComparisonCondition("status", ComparisonOperation.EQUAL, "deleted");
        OrCondition orCondition = new OrCondition(left, right);
        
        boolean result = orCondition.accept(mockVisitor);
        assertFalse(result, "false OR false should return false");
        assertTrue(mockVisitor.visitCount >= 2);
    }

    @Test
    public void testAccept_NestedOrConditions() {
        // Test: (cond1 OR cond2) OR cond3 - at least one true
        mockVisitor.setResultSequence(false, true, false);
        
        ComparisonCondition cond1 = new ComparisonCondition("role", ComparisonOperation.EQUAL, "admin");
        ComparisonCondition cond2 = new ComparisonCondition("role", ComparisonOperation.EQUAL, "moderator");
        ComparisonCondition cond3 = new ComparisonCondition("role", ComparisonOperation.EQUAL, "editor");
        
        OrCondition innerOr = new OrCondition(cond1, cond2);
        OrCondition outerOr = new OrCondition(innerOr, cond3);
        
        boolean result = outerOr.accept(mockVisitor);
        assertTrue(result, "(false OR true) OR false should return true");
    }

    @Test
    public void testAccept_NestedOrConditions_AllFalse() {
        // Test: (cond1 OR cond2) OR cond3 - all false
        mockVisitor.setResultSequence(false, false, false);
        
        ComparisonCondition cond1 = new ComparisonCondition("status", ComparisonOperation.EQUAL, "deleted");
        ComparisonCondition cond2 = new ComparisonCondition("status", ComparisonOperation.EQUAL, "archived");
        ComparisonCondition cond3 = new ComparisonCondition("status", ComparisonOperation.EQUAL, "suspended");
        
        OrCondition innerOr = new OrCondition(cond1, cond2);
        OrCondition outerOr = new OrCondition(innerOr, cond3);
        
        boolean result = outerOr.accept(mockVisitor);
        assertFalse(result, "(false OR false) OR false should return false");
    }

    @Test
    public void testAccept_NestedOrConditions_AllTrue() {
        // Test: (cond1 OR cond2) OR cond3 - all true
        mockVisitor.setResultSequence(true, true, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("valid", ComparisonOperation.EQUAL, "true");
        ComparisonCondition cond2 = new ComparisonCondition("active", ComparisonOperation.EQUAL, "true");
        ComparisonCondition cond3 = new ComparisonCondition("enabled", ComparisonOperation.EQUAL, "true");
        
        OrCondition innerOr = new OrCondition(cond1, cond2);
        OrCondition outerOr = new OrCondition(innerOr, cond3);
        
        boolean result = outerOr.accept(mockVisitor);
        assertTrue(result, "(true OR true) OR true should return true");
    }

    @Test
    public void testAccept_ComplexNesting() {
        // Test: cond1 OR (cond2 OR cond3)
        mockVisitor.setResultSequence(false, false, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("priority", ComparisonOperation.EQUAL, "high");
        ComparisonCondition cond2 = new ComparisonCondition("priority", ComparisonOperation.EQUAL, "urgent");
        ComparisonCondition cond3 = new ComparisonCondition("priority", ComparisonOperation.EQUAL, "critical");
        
        OrCondition innerOr = new OrCondition(cond2, cond3);
        OrCondition outerOr = new OrCondition(cond1, innerOr);
        
        boolean result = outerOr.accept(mockVisitor);
        assertTrue(result, "false OR (false OR true) should return true");
    }

    @Test
    public void testAccept_ThreeConditionsChained_AllTrue() {
        // Test: (cond1 OR cond2) OR cond3 - all true
        mockVisitor.setResultSequence(true, true, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("field1", ComparisonOperation.EQUAL, "value1");
        ComparisonCondition cond2 = new ComparisonCondition("field2", ComparisonOperation.EQUAL, "value2");
        ComparisonCondition cond3 = new ComparisonCondition("field3", ComparisonOperation.EQUAL, "value3");
        
        OrCondition or1 = new OrCondition(cond1, cond2);
        OrCondition or2 = new OrCondition(or1, cond3);
        
        boolean result = or2.accept(mockVisitor);
        assertTrue(result);
    }

    @Test
    public void testAccept_ThreeConditionsChained_OnlyLastTrue() {
        // Test: (cond1 OR cond2) OR cond3 - only last true
        mockVisitor.setResultSequence(false, false, true);
        
        ComparisonCondition cond1 = new ComparisonCondition("field1", ComparisonOperation.EQUAL, "wrong1");
        ComparisonCondition cond2 = new ComparisonCondition("field2", ComparisonOperation.EQUAL, "wrong2");
        ComparisonCondition cond3 = new ComparisonCondition("field3", ComparisonOperation.EQUAL, "correct");
        
        OrCondition or1 = new OrCondition(cond1, cond2);
        OrCondition or2 = new OrCondition(or1, cond3);
        
        boolean result = or2.accept(mockVisitor);
        assertTrue(result);
    }

    @Test
    public void testAccept_UsesDefaultTrueWhenNoSequenceProvided() {
        // No call to setResultSequence -> results == null, fallback branch in visitComparison
        ComparisonCondition left = new ComparisonCondition("any", ComparisonOperation.EQUAL, "x");
        ComparisonCondition right = new ComparisonCondition("any", ComparisonOperation.EQUAL, "y");
        OrCondition orCondition = new OrCondition(left, right);

        boolean result = orCondition.accept(mockVisitor);

        // Fallback path returns true and increments visitCount
        assertTrue(result);
        assertTrue(mockVisitor.visitCount >= 1);
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
                boolean result = results[visitCount];
                visitCount++;
                return result;
            }
            // No sequence or sequence exhausted: still count the visit
            visitCount++;
            return true;
        }
    }
}
