package org.nanodb.statement.condition;

import org.junit.jupiter.api.Test;
import org.nanodb.db.Row;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionTest {

    @Test
    public void testComparisonConditionEqualityIntId() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionInequalityIntId() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "3");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionGreaterThanId() {
        Row row = new Row(10, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", ">", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionGreaterThanIdFalse() {
        Row row = new Row(3, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", ">", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionLessThanId() {
        Row row = new Row(2, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "<", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionLessThanIdFalse() {
        Row row = new Row(10, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "<", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionInvalidOperatorForId() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "!=", "5");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionInvalidIdValue() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("id", "=", "abc");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseName() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseNameNoMatch() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", "=", "Physics");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseCode() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "=", "CS3343");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseCodeNoMatch() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "=", "PHY101");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseNameInvalidOperator() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseName", ">", "Software-Engineering-Practice");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCourseCodeInvalidOperator() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("courseCode", "<", "CS3343");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionUnknownColumn() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("unknownColumn", "=", "value");
        assertFalse(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCaseInsensitiveId() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("ID", "=", "5");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testComparisonConditionCaseInsensitiveCourseName() {
        Row row = new Row(1, "Software-Engineering-Practice", "CS3343");
        Condition condition = new ComparisonCondition("COURSENAME", "=", "Software-Engineering-Practice");
        assertTrue(condition.evaluate(row));
    }

    @Test
    public void testAndCondition() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "5");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition andCondition = new AndCondition(cond1, cond2);
        assertTrue(andCondition.evaluate(row));
    }

    @Test
    public void testAndConditionFirstFalse() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "3");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition andCondition = new AndCondition(cond1, cond2);
        assertFalse(andCondition.evaluate(row));
    }

    @Test
    public void testAndConditionSecondFalse() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "5");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition andCondition = new AndCondition(cond1, cond2);
        assertFalse(andCondition.evaluate(row));
    }

    @Test
    public void testAndConditionBothFalse() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "3");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition andCondition = new AndCondition(cond1, cond2);
        assertFalse(andCondition.evaluate(row));
    }

    @Test
    public void testOrCondition() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "5");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition orCondition = new OrCondition(cond1, cond2);
        assertTrue(orCondition.evaluate(row));
    }

    @Test
    public void testOrConditionFirstTrue() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "5");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition orCondition = new OrCondition(cond1, cond2);
        assertTrue(orCondition.evaluate(row));
    }

    @Test
    public void testOrConditionSecondTrue() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "3");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Software-Engineering-Practice");
        Condition orCondition = new OrCondition(cond1, cond2);
        assertTrue(orCondition.evaluate(row));
    }

    @Test
    public void testOrConditionBothFalse() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "3");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition orCondition = new OrCondition(cond1, cond2);
        assertFalse(orCondition.evaluate(row));
    }

    @Test
    public void testComplexConditionAndOr() {
        Row row = new Row(5, "Software-Engineering-Practice", "CS3343");
        Condition cond1 = new ComparisonCondition("id", "=", "5");
        Condition cond2 = new ComparisonCondition("courseName", "=", "Physics");
        Condition cond3 = new ComparisonCondition("courseCode", "=", "CS3343");
        Condition andCond = new AndCondition(cond2, cond3);
        Condition orCond = new OrCondition(cond1, andCond);
        assertTrue(orCond.evaluate(row));
    }
}
