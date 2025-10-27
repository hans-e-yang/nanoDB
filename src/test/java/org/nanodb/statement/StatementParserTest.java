package org.nanodb.statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.db.Row;

import static org.junit.jupiter.api.Assertions.*;

public class StatementParserTest {
    private Statement statement;

    @BeforeEach
    public void setUp() {
        statement = new Statement();
    }

    // ==================== CREATE STATEMENTS ====================
    // Tests for CREATE TABLE commands

    @Test
    public void testCreateTable() {
        String tableName = "courses";

        PrepareResult result = StatementParser.prepareStatement(String.format("create table %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertEquals(tableName, statement.tableName);
    }

    @Test
    public void testCreateTableWithExtraSpaces() {
        String tableName = "courses";

        PrepareResult result = StatementParser.prepareStatement(String.format("create table  %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertEquals(tableName, statement.tableName);
    }

    @Test
    public void testCreateTableCaseInsensitive() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("CREATE TABLE %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertEquals(tableName, statement.tableName);
    }

    // ==================== DROP STATEMENTS ====================
    // Tests for DROP TABLE commands

    @Test
    public void testDropTable() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("drop table %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DROP_TABLE, statement.type);
        assertEquals(tableName, statement.tableName);
    }

    @Test
    public void testDropTableWithExtraSpaces() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("  drop table   %s  ", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DROP_TABLE, statement.type);
        assertEquals(tableName, statement.tableName);
    }

    // ==================== INSERT STATEMENTS ====================
    // Tests for INSERT commands

    @Test
    public void testInsertBasic() {
        String tableName = "courses";
        int id = 1;
        String courseName = "Software-Engineering-Practice";
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, courseName, courseCode), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.INSERT, statement.type);
        assertEquals(tableName, statement.tableName);
        assertNotNull(statement.rowToInsert);
        assertEquals(id, statement.rowToInsert.id);
        assertEquals(courseName, statement.rowToInsert.courseName);
        assertEquals(courseCode, statement.rowToInsert.courseCode);
    }

    @Test
    public void testInsertWithLargeId() {
        String tableName = "courses";
        int id = 999;
        String courseName = "Software-Engineering-Practice";
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, courseName, courseCode), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(id, statement.rowToInsert.id);
    }

    @Test
    public void testInsertNegativeId() {
        String tableName = "courses";
        int id = -1;
        String courseName = "Software-Engineering-Practice";
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, courseName, courseCode), statement);
        assertEquals(PrepareResult.NEGATIVE_ID, result);
    }

    @Test
    public void testInsertInvalidId() {
        String tableName = "courses";
        String invalidId = "abc";
        String courseName = "Software-Engineering-Practice";
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %s %s %s", tableName, invalidId, courseName, courseCode), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsertTooFewArguments() {
        String tableName = "courses";
        int id = 1;
        String courseName = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s", tableName, id, courseName), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsertCourseNameTooLong() {
        String tableName = "courses";
        int id = 1;
        String longName = "a".repeat(Row.COURSE_NAME_SIZE + 1);
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, longName, courseCode), statement);
        assertEquals(PrepareResult.STRING_TOO_LONG, result);
    }

    @Test
    public void testInsertCourseCodeTooLong() {
        String tableName = "courses";
        int id = 1;
        String courseName = "Software-Engineering-Practice";
        String longCode = "a".repeat(Row.COURSE_CODE_SIZE + 1);
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, courseName, longCode), statement);
        assertEquals(PrepareResult.STRING_TOO_LONG, result);
    }

    @Test
    public void testInsertCourseNameMaxLength() {
        String tableName = "courses";
        int id = 1;
        String maxName = "a".repeat(Row.COURSE_NAME_SIZE);
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("insert into %s %d %s %s", tableName, id, maxName, courseCode), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(maxName, statement.rowToInsert.courseName);
    }

    // ==================== SELECT STATEMENTS ====================
    // Tests for SELECT commands

    @Test
    public void testSelectAllColumns() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.SELECT, statement.type);
        assertEquals(tableName, statement.tableName);
        assertNull(statement.columns);
    }

    @Test
    public void testSelectSpecificColumns() {
        String tableName = "courses";
        String columns = "id, courseName";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select %s from %s", columns, tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.SELECT, statement.type);
        assertEquals(tableName, statement.tableName);
        assertNotNull(statement.columns);
        assertEquals(2, statement.columns.size());
        assertEquals("id", statement.columns.get(0));
        assertEquals("courseName", statement.columns.get(1));
    }

    @Test
    public void testSelectSingleColumn() {
        String tableName = "courses";
        String column = "courseName";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select %s from %s", column, tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(1, statement.columns.size());
        assertEquals(column, statement.columns.get(0));
    }

    @Test
    public void testSelectWithWhereCondition() {
        String tableName = "courses";
        int id = 1;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(tableName, statement.tableName);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelectMissingFrom() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * %s", tableName), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testSelectWithExtraSpaces() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("  select  *  from   %s  ", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(tableName, statement.tableName);
    }

    // ==================== UPDATE STATEMENTS ====================
    // Tests for UPDATE commands

    @Test
    public void testUpdateSingleColumn() {
        String tableName = "courses";
        String column = "courseName";
        String value = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, value), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.UPDATE, statement.type);
        assertEquals(tableName, statement.tableName);
        assertEquals(1, statement.setValues.size());
        assertEquals(value, statement.setValues.get(column));
    }

    @Test
    public void testUpdateMultipleColumns() {
        String tableName = "courses";
        String column1 = "courseName";
        String value1 = "Software-Engineering-Practice";
        String column2 = "courseCode";
        String value2 = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s, %s = %s", tableName, column1, value1, column2, value2), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(2, statement.setValues.size());
        assertEquals(value1, statement.setValues.get(column1));
        assertEquals(value2, statement.setValues.get(column2));
    }

    @Test
    public void testUpdateWithQuotedValue() {
        String tableName = "courses";
        String column = "courseName";
        String value = "'Software Engineering Practice'";
        String expectedValue = "Software Engineering Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, value), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(expectedValue, statement.setValues.get(column));
    }

    @Test
    public void testUpdateWithWhereCondition() {
        String tableName = "courses";
        String column = "courseName";
        String value = "Software-Engineering-Practice";
        int whereId = 1;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s where id = %d", tableName, column, value, whereId), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(value, statement.setValues.get(column));
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testUpdateMissingSet() {
        String tableName = "courses";
        String column = "courseName";
        String value = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s %s = %s", tableName, column, value), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdateInvalidFormat() {
        String tableName = "courses";
        String column = "courseName";
        String value = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s %s", tableName, column, value), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    // ==================== DELETE STATEMENTS ====================
    // Tests for DELETE commands

    @Test
    public void testDeleteAll() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("delete from %s", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DELETE, statement.type);
        assertEquals(tableName, statement.tableName);
        assertNull(statement.whereCondition);
    }

    @Test
    public void testDeleteWithWhereCondition() {
        String tableName = "courses";
        int id = 1;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("delete from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(tableName, statement.tableName);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testDeleteWithExtraSpaces() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("  delete from   %s  ", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(tableName, statement.tableName);
    }

    // ==================== ERROR HANDLING ====================
    // Tests for invalid statements

    @Test
    public void testUnrecognizedStatement() {
        String input = "invalid statement";
        
        PrepareResult result = StatementParser.prepareStatement(input, statement);
        assertEquals(PrepareResult.UNRECOGNIZED_STATEMENT, result);
    }

    @Test
    public void testEmptyStatement() {
        String input = "";
        
        PrepareResult result = StatementParser.prepareStatement(input, statement);
        assertEquals(PrepareResult.UNRECOGNIZED_STATEMENT, result);
    }

    @Test
    public void testNullStatement() {
        String input = "   ";
        
        PrepareResult result = StatementParser.prepareStatement(input, statement);
        assertEquals(PrepareResult.UNRECOGNIZED_STATEMENT, result);
    }

    // ==================== PARSECOMP TESTS ====================
    // Tests for parseComp method

    @Test
    public void testParseCompEqualityId() {
        String tableName = "courses";
        int id = 5;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompGreaterThanId() {
        String tableName = "courses";
        int id = 10;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id > %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompLessThanId() {
        String tableName = "courses";
        int id = 100;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id < %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompEqualityCourseName() {
        String tableName = "courses";
        String courseName = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, courseName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompEqualityCourseCode() {
        String tableName = "courses";
        String courseCode = "CS3343";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseCode = %s", tableName, courseCode), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedStringValue() {
        String tableName = "courses";
        String courseName = "'Software Engineering Practice'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, courseName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompMissingOperator() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id 5", tableName), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testParseCompWhitespaceAroundOperator() {
        String tableName = "courses";
        int id = 5;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id  =  %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompMultipleComparisons() {
        String tableName = "courses";
        int id = 5;
        String courseName = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d and courseName = %s", tableName, id, courseName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompMultipleOrComparisons() {
        String tableName = "courses";
        int id = 5;
        String courseName = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d or courseName = %s", tableName, id, courseName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompComplexCondition() {
        String tableName = "courses";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id > 5 and courseName = Software-Engineering-Practice or courseCode = CS3343", tableName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompCaseInsensitiveColumnName() {
        String tableName = "courses";
        int id = 5;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where ID = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompInUpdateStatement() {
        String tableName = "courses";
        String column = "courseName";
        String value = "Software-Engineering-Practice";
        int whereId = 1;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s where id = %d", tableName, column, value, whereId), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompInDeleteStatement() {
        String tableName = "courses";
        int id = 5;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("delete from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompZeroValue() {
        String tableName = "courses";
        int id = 0;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompLargeValue() {
        String tableName = "courses";
        int id = 999999;
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where id = %d", tableName, id), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompCourseNameGreaterThanFails() {
        String tableName = "courses";
        String courseName = "Software-Engineering-Practice";
        
        // String comparison with > should parse successfully, but evaluate to false for courseName
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName > %s", tableName, courseName), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompCourseCodeLessThanFails() {
        String tableName = "courses";
        String courseCode = "CS3343";
        
        // String comparison with < should parse successfully, but evaluate to false for courseCode
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseCode < %s", tableName, courseCode), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    // ==================== QUOTING TESTS ====================
    // Tests for unquoting logic in UPDATE values and WHERE conditions

    @Test
    public void testUpdateQuotedValueUnquoted() {
        String tableName = "courses";
        String column = "courseName";
        String quotedValue = "'Software Engineering Practice'";
        String expectedValue = quotedValue.replace("'", "");
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(expectedValue, statement.setValues.get(column));
        assertTrue(statement.setValues.get(column).startsWith("S"));
    }

    @Test
    public void testUpdateQuotedValueWithStartQuoteOnly() {
        String tableName = "courses";
        String column = "courseName";
        String valueWithStartQuoteOnly = "'Software";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, valueWithStartQuoteOnly), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(valueWithStartQuoteOnly, statement.setValues.get(column));
    }

    @Test
    public void testUpdateQuotedValueWithEndQuoteOnly() {
        String tableName = "courses";
        String column = "courseName";
        String valueWithEndQuoteOnly = "Engineering'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, valueWithEndQuoteOnly), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(valueWithEndQuoteOnly, statement.setValues.get(column));
    }

    @Test
    public void testUpdateMultipleQuotedValues() {
        String tableName = "courses";

        String column1 = "courseName";
        String quotedValue1 = "'Software Engineering Practice'";
        String expectedValue1 = quotedValue1.replace("'", "");
        
        String column2 = "courseCode";
        String quotedValue2 = "'CS3343'";
        String expectedValue2 = quotedValue2.replace("'", "");
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s, %s = %s", tableName, column1, quotedValue1, column2, quotedValue2), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(expectedValue1, statement.setValues.get(column1));
        assertEquals(expectedValue2, statement.setValues.get(column2));
    }

    @Test
    public void testUpdateUnquotedValue() {
        String tableName = "courses";
        String column = "courseName";
        String unquotedValue = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, unquotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("Software-Engineering-Practice", statement.setValues.get(column));
    }

    @Test
    public void testUpdateQuotedValueWithAccpetedSpecialCharacters() {
        String tableName = "courses";
        String column = "courseName";
        String quotedValue = "'!@#$%&*^()-+_~><[]{}/;:?.'";
        String expectedValue = quotedValue.replace("'", "");
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(expectedValue, statement.setValues.get(column));
    }

        @Test
    public void testUpdateQuotedValueWithUnaccpetedSpecialCharacters() {
        String tableName = "courses";
        String column = "courseName";
        String quotedValue = "'='";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("update %s set %s = %s", tableName, column, quotedValue), statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testParseCompQuotedStringWithSpacesValue() {
        String tableName = "courses";
        String quotedValue = "'Software Engineering Practice'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedStringWithStartQuoteOnly() {
        String tableName = "courses";
        String quotedValue = "'Software Engineering Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedStringWithEndQuoteOnly() {
        String tableName = "courses";
        String quotedValue = "Software Engineering Practice'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompMultipleQuotedConditions() {
        String tableName = "courses";
        String quotedValue1 = "'Software-Engineering-Practice'";
        String quotedValue2 = "'CS3343'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s and courseCode = %s", tableName, quotedValue1, quotedValue2), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedValueWithoutSpaces() {
        String tableName = "courses";
        String quotedValue = "'Software-Engineering-Practice'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedValueWithSpecialChars() {
        String tableName = "courses";
        String quotedValue = "'!@#$%&*^()-+_~><[]{}/;:?.'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseCode = %s", tableName, quotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompUnquotedStringValue() {
        String tableName = "courses";
        String unquotedValue = "Software-Engineering-Practice";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s", tableName, unquotedValue), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseCompQuotedInOrCondition() {
        String tableName = "courses";
        String quotedValue1 = "'Software-Engineering-Practice'";
        String quotedValue2 = "'CS3343'";
        
        PrepareResult result = StatementParser.prepareStatement(String.format("select * from %s where courseName = %s or courseCode = %s", tableName, quotedValue1, quotedValue2), statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }
}
