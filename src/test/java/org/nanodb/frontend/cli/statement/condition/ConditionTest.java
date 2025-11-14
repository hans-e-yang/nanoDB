package org.nanodb.frontend.cli.statement.condition;

import org.junit.jupiter.api.Test;
import org.nanodb.backend.db.Row;
import org.nanodb.frontend.cli.statement.condition.AndCondition;
import org.nanodb.frontend.cli.statement.condition.ComparisonCondition;
import org.nanodb.frontend.cli.statement.condition.Condition;
import org.nanodb.frontend.cli.statement.condition.OrCondition;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimum test set for 100% Condition Coverage (CC), Decision Coverage (DC), and Branch Coverage (BC)
 * 
 * Coverage Analysis:
 * 
 * ComparisonCondition.evaluate():
 * - Decision 1: column.equalsIgnoreCase("id") - needs T/F
 * - Decision 2: column.equalsIgnoreCase("courseName") - needs T/F
 * - Decision 3: column.equalsIgnoreCase("courseCode") - needs T/F
 * - Decision 4 (if id): try-catch for Integer.parseInt() - needs success/exception
 * - Decision 5 (if id): switch(op) = "=", ">", "<", default - needs all 4 branches
 * - Decision 6 (if id, op="="): rowVal == valInt - needs T/F
 * - Decision 7 (if id, op=">"): rowVal > valInt - needs T/F
 * - Decision 8 (if id, op="<"): rowVal < valInt - needs T/F
 * - Decision 9 (if courseName): !op.equals("=") - needs T/F
 * - Decision 10 (if courseName): row.courseName.equals(value) - needs T/F
 * - Decision 11 (if courseCode): !op.equals("=") - needs T/F
 * - Decision 12 (if courseCode): row.courseCode.equals(value) - needs T/F
 * 
 * AndCondition.evaluate():
 * - Decision 13: left.evaluate(row) && right.evaluate(row) - needs TT, TF, FT, FF (but && short-circuits, so TT, F* needed)
 * 
 * OrCondition.evaluate():
 * - Decision 14: left.evaluate(row) || right.evaluate(row) - needs TT, TF, FT, FF (but || short-circuits, so T*, FF needed)
 * 
 * Minimum test cases: 16 tests
 */
public class ConditionTest {

    // ==================== COMPARISONCONDIT ION: ID COLUMN ====================
    // Covers: column="id" branch (D1=T), all operators, true/false outcomes

    @Test
    public void testIdEquals_True() {
        // Covers: D1=T, D4=success, D5=case"=", D6=T
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testIdEquals_False() {
        // Covers: D1=T, D4=success, D5=case"=", D6=F
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "3");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testIdGreaterThan_True() {
        // Covers: D1=T, D4=success, D5=case">", D7=T
        Row row = new Row(10, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", ">", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testIdGreaterThan_False() {
        // Covers: D1=T, D4=success, D5=case">", D7=F
        Row row = new Row(3, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", ">", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testIdLessThan_True() {
        // Covers: D1=T, D4=success, D5=case"<", D8=T
        Row row = new Row(2, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "<", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testIdLessThan_False() {
        // Covers: D1=T, D4=success, D5=case"<", D8=F
        Row row = new Row(10, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "<", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testIdInvalidOperator() {
        // Covers: D1=T, D4=success, D5=default
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "!=", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testIdInvalidValue_NumberFormatException() {
        // Covers: D1=T, D4=exception (catch branch)
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "a");
        assertFalse(condition.evaluate(row));
    }

    // ==================== COMPARISONCONDIT ION: COURSENAME COLUMN ====================
    // Covers: column="courseName" branch (D2=T)

    @Test
    public void testCourseNameEquals_True() {
        // Covers: D1=F, D2=T, D9=F, D10=T
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testCourseNameEquals_False() {
        // Covers: D1=F, D2=T, D9=F, D10=F
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", "=", "AI-for-Software-Engineering");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testCourseNameInvalidOperator() {
        // Covers: D1=F, D2=T, D9=T
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", ">", "Software-Engineering-Practice");
        assertFalse(condition.evaluate(row));
    }

    // ==================== COMPARISONCONDIT ION: COURSECODE COLUMN ====================
    // Covers: column="courseCode" branch (D3=T)

    @Test
    public void testCourseCodeEquals_True() {
        // Covers: D1=F, D2=F, D3=T, D11=F, D12=T
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "=", "CS3343");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testCourseCodeEquals_False() {
        // Covers: D1=F, D2=F, D3=T, D11=F, D12=F
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "=", "CS3383");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testCourseCodeInvalidOperator() {
        // Covers: D1=F, D2=F, D3=T, D11=T
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "<", "CS3343");
        assertFalse(condition.evaluate(row));
    }

    // ==================== COMPARISONCONDIT ION: UNKNOWN COLUMN ====================
    // Covers: all column checks false (D1=F, D2=F, D3=F)

    @Test
    public void testUnknownColumn() {
        // Covers: D1=F, D2=F, D3=F, returns false
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("unknownColumn", "=", "value");
        assertFalse(condition.evaluate(row));
    }

    // ==================== ANDCONDITION ====================
    // Covers: left && right with TT, F* (short-circuit on first false)

    @Test
    public void testAndCondition_BothTrue() {
        // Covers: D13: left=T, right=T → result=T
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "5");
        Condition right = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition andCondition = new AndCondition(left, right);
        assertTrue(andCondition.evaluate(row));
    }

    @Test
    public void testAndCondition_LeftFalse() {
        // Covers: D13: left=F (short-circuit, right not evaluated) → result=F
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "3");
        Condition right = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition andCondition = new AndCondition(left, right);
        assertFalse(andCondition.evaluate(row));
    }

    @Test
    public void testAndCondition_RightFalse() {
        // Covers: D13: left=T, right=F → result=F
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "5");
        Condition right = new ComparisonCondition("courseName", "=", "AI-for-Software-Engineering");
        Condition andCondition = new AndCondition(left, right);
        assertFalse(andCondition.evaluate(row));
    }

    // ==================== ORCONDITION ====================
    // Covers: left || right with T*, FF (short-circuit on first true)

    @Test
    public void testOrCondition_LeftTrue() {
        // Covers: D14: left=T (short-circuit, right not evaluated) → result=T
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "5");
        Condition right = new ComparisonCondition("courseName", "=", "AI-for-Software-Engineering");
        Condition orCondition = new OrCondition(left, right);
        assertTrue(orCondition.evaluate(row));
    }

    @Test
    public void testOrCondition_RightTrue() {
        // Covers: D14: left=F, right=T → result=T
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "3");
        Condition right = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition orCondition = new OrCondition(left, right);
        assertTrue(orCondition.evaluate(row));
    }

    @Test
    public void testOrCondition_BothFalse() {
        // Covers: D14: left=F, right=F → result=F
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition left = new ComparisonCondition("id", "=", "3");
        Condition right = new ComparisonCondition("courseName", "=", "AI-for-Software-Engineering");
        Condition orCondition = new OrCondition(left, right);
        assertFalse(orCondition.evaluate(row));
    }
}
