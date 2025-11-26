package org.nanodb.frontend.cli.statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.cli.statement.PrepareResult;
import org.nanodb.cli.statement.Statement;
import org.nanodb.cli.statement.StatementParser;
import org.nanodb.cli.statement.StatementType;
import org.nanodb.internal.condition.BooleanCondition;
import org.nanodb.internal.primitives.DateType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for StatementParser
 * Achieves 96%+ branch coverage testing SQL statement parsing
 *
 * Test Organization:
 * 1. CREATE TABLE - Table creation with schema validation
 * 2. DROP TABLE - Table deletion
 * 3. INSERT - Row insertion with value validation
 * 4. SELECT - Query parsing with column selection and WHERE clauses
 * 5. UPDATE - Row updates with SET and WHERE clauses
 * 6. DELETE - Row deletion with WHERE clauses
 * 7. WHERE Conditions - Comparison operators and logical combinations
 * 8. Edge Cases - Syntax errors and boundary conditions
 */
public class StatementParserTest {
    private Statement statement;

    @BeforeEach
    public void setUp() {
        statement = new Statement();
    }

    // ==================== CREATE TABLE Tests ====================

    @Test
    public void testCreateTable_NoColumnDefinitions() {
        // Test CREATE TABLE without column definitions (should return syntax error)
        PrepareResult result = StatementParser.prepareStatement("create table courses", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_WithSchema() {
        // Test CREATE TABLE with multiple columns (int and varchar types)
        PrepareResult result = StatementParser.prepareStatement("create table courses (id int, courseName varchar(50), courseCode varchar(10))", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertEquals("courses", statement.tableName);
        assertNotNull(statement.tableSchema);
        assertEquals(3, statement.tableSchema.getFields().size());
    }

    @Test
    public void testCreateTable_WithDateType() {
        // Test CREATE TABLE with date data type
        PrepareResult result = StatementParser.prepareStatement("create table events (id int, eventDate date)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertNotNull(statement.tableSchema);
        assertTrue(statement.tableSchema.getFields().get(1).getValue() instanceof DateType);
    }

    @Test
    public void testCreateTable_WithSpaces() {
        // Test parser handles extra whitespace correctly
        PrepareResult result = StatementParser.prepareStatement("create table   courses   (  id   int  ,  name   varchar(50)  )", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.CREATE_TABLE, statement.type);
        assertEquals(2, statement.tableSchema.getFields().size());
    }

    @Test
    public void testCreateTable_InvalidSchema() {
        // Missing type specification for column
        PrepareResult result = StatementParser.prepareStatement("create table courses (id)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_InvalidVarcharLength() {
        // Non-numeric varchar length
        PrepareResult result = StatementParser.prepareStatement("create table courses (name varchar(abc))", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_VarcharNegativeLength() {
        // Negative varchar length not allowed
        PrepareResult result = StatementParser.prepareStatement("create table courses (name varchar(-5))", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_VarcharZeroLength() {
        // Zero varchar length not allowed
        PrepareResult result = StatementParser.prepareStatement("create table courses (name varchar(0))", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_InvalidType() {
        // Unsupported data type
        PrepareResult result = StatementParser.prepareStatement("create table courses (name blob)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_MissingClosingParen() {
        // Missing closing parenthesis in schema
        PrepareResult result = StatementParser.prepareStatement("create table courses (id int, name varchar(50)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testCreateTable_TooManyTypeParts() {
        // Extra tokens in column definition
        PrepareResult result = StatementParser.prepareStatement("create table courses (id int extra)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    // ==================== DROP TABLE Tests ====================

    @Test
    public void testDropTable() {
        // Test DROP TABLE statement parsing
        PrepareResult result = StatementParser.prepareStatement("drop table courses", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DROP_TABLE, statement.type);
        assertEquals("courses", statement.tableName);
    }

    // ==================== INSERT Tests ====================

    @Test
    public void testInsert_Success() {
        // Test basic INSERT with multiple columns (unquoted values)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, courseName, courseCode) values (1, Software-Engineering-Practice, CS3343)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.INSERT, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals("1", statement.valuesToInsert.get("id"));
        assertEquals("Software-Engineering-Practice", statement.valuesToInsert.get("courseName"));
        assertEquals("CS3343", statement.valuesToInsert.get("courseCode"));
    }

    @Test
    public void testInsert_WithQuotes() {
        // Test INSERT with quoted values (handles spaces in values)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, courseName, courseCode) values (1, 'Software Engineering Practice', 'CS3343')", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("Software Engineering Practice", statement.valuesToInsert.get("courseName"));
        assertEquals("CS3343", statement.valuesToInsert.get("courseCode"));
    }

    @Test
    public void testInsert_SingleColumn() {
        // Test INSERT with single column
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id) values (1)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.INSERT, statement.type);
        assertEquals("1", statement.valuesToInsert.get("id"));
    }

    @Test
    public void testInsert_WithoutSpaces() {
        // Test parser handles minimal whitespace
        PrepareResult result = StatementParser.prepareStatement("insert into courses(id,name)values(1,test)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("1", statement.valuesToInsert.get("id"));
        assertEquals("test", statement.valuesToInsert.get("name"));
    }

    @Test
    public void testInsert_MismatchedColumns() {
        // More values than columns (unquoted)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, courseName) values (1, Software-Engineering-Practice, CS3343)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_TooManyValues() {
        // More values than columns (quoted)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) values (1, 'test', 'extra')", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_TooFewValues() {
        // Fewer values than columns
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name, age) values (1, 'test')", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_MissingParentheses() {
        // Missing parentheses around columns
        PrepareResult result = StatementParser.prepareStatement("insert into courses id, courseName values 1, Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_MissingValues() {
        // Missing VALUES clause entirely
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, courseName)", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_MissingValuesParentheses() {
        // VALUES keyword without parentheses
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, courseName) values", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_NoValuesKeyword() {
        // Missing VALUES keyword
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) (1, 'test')", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_MissingValuesParenthesis() {
        // Missing closing parenthesis in values
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) values (1, 'test'", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testInsert_NullValue() {
        // Test INSERT with null keyword (lowercase)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) values (1, null)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.INSERT, statement.type);
        assertEquals("1", statement.valuesToInsert.get("id"));
        assertNull(statement.valuesToInsert.get("name"));
        assertTrue(statement.valuesToInsert.containsKey("name"));
    }

    @Test
    public void testInsert_NullValueUpperCase() {
        // Test INSERT with NULL keyword (uppercase)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) values (1, NULL)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNull(statement.valuesToInsert.get("name"));
        assertTrue(statement.valuesToInsert.containsKey("name"));
    }

    @Test
    public void testInsert_NullValueMixedCase() {
        // Test INSERT with Null keyword (mixed case)
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name) values (1, Null)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNull(statement.valuesToInsert.get("name"));
    }

    @Test
    public void testInsert_MultipleNullValues() {
        // Test INSERT with multiple null values
        PrepareResult result = StatementParser.prepareStatement("insert into courses (id, name, code) values (null, null, null)", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNull(statement.valuesToInsert.get("id"));
        assertNull(statement.valuesToInsert.get("name"));
        assertNull(statement.valuesToInsert.get("code"));
    }

    // ==================== SELECT Tests ====================

    @Test
    public void testSelect_AllColumns() {
        // Test SELECT * (all columns)
        PrepareResult result = StatementParser.prepareStatement("select * from courses", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.SELECT, statement.type);
        assertEquals("courses", statement.tableName);
        assertNull(statement.columns); // null indicates all columns
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testSelect_SpecificColumns() {
        // Test SELECT with specific column list
        PrepareResult result = StatementParser.prepareStatement("select id, courseName from courses", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(2, statement.columns.size());
        assertEquals("id", statement.columns.get(0));
        assertEquals("courseName", statement.columns.get(1));
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testSelect_ColumnSpecialCharacters() {
        // Test column names with underscores and numbers
        PrepareResult result = StatementParser.prepareStatement("select col_1, col2 from courses", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(2, statement.columns.size());
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testSelect_WithWhere() {
        // Test SELECT with simple WHERE clause
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelect_WithAndCondition() {
        // Test SELECT with AND in WHERE clause
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 and courseName = Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelect_WithOrCondition() {
        // Test SELECT with OR in WHERE clause
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 or courseName = Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelect_WithMultipleOrConditions() {
        // Test SELECT with multiple OR conditions
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 or id = 2 or id = 3", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelect_WithMultipleAndConditions() {
        // Test SELECT with multiple AND conditions
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 and courseName = Software and courseCode = CS3343", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testSelect_MissingFrom() {
        // Missing FROM keyword
        PrepareResult result = StatementParser.prepareStatement("select * courses", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testSelect_InvalidWhereCondition() {
        // Invalid WHERE condition (no operator)
        PrepareResult result = StatementParser.prepareStatement("select * from courses where invalid", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    // ==================== UPDATE Tests ====================

    @Test
    public void testUpdate_Success() {
        // Test basic UPDATE with single column
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.UPDATE, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals("Software-Engineering-Practice", statement.setValues.get("courseName"));
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testUpdate_QuotedValue() {
        // Test UPDATE with quoted value
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = 'Software Engineering Practice'", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("Software Engineering Practice", statement.setValues.get("courseName"));
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testUpdate_MultipleColumns() {
        // Test UPDATE with multiple columns
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName, courseCode = CS1234", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("NewName", statement.setValues.get("courseName"));
        assertEquals("CS1234", statement.setValues.get("courseCode"));
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testUpdate_WithWhere() {
        // Test UPDATE with WHERE clause
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = AI-for-Software-Engineering where id = 1", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testUpdate_WithMultipleAndConditions() {
        // Test UPDATE with multiple AND conditions in WHERE
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName where id = 1 and courseCode = CS3343", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testUpdate_WithMultipleOrConditions() {
        // Test UPDATE with OR conditions in WHERE
        PrepareResult result = StatementParser.prepareStatement("update courses set name = 'Updated' where id = 1 or id = 2", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testUpdate_WithoutSpaces() {
        // Test parser handles minimal whitespace
        PrepareResult result = StatementParser.prepareStatement("update courses set name=test,age=25", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals("test", statement.setValues.get("name"));
        assertEquals("25", statement.setValues.get("age"));
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testUpdate_MissingSet() {
        // Missing SET keyword
        PrepareResult result = StatementParser.prepareStatement("update courses courseName = Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_InvalidFormat() {
        // Missing equals sign in SET clause
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName Software-Engineering-Practice", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_MissingValue() {
        // Missing value after equals sign
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName =", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_EmptySetValues() {
        // Empty SET clause
        PrepareResult result = StatementParser.prepareStatement("update courses set", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_InvalidWhereCondition() {
        // Invalid WHERE condition
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName where badcondition", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_NullValue() {
        // Test UPDATE with null keyword (lowercase)
        PrepareResult result = StatementParser.prepareStatement("update courses set name = null where id = 1", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.UPDATE, statement.type);
        assertNull(statement.setValues.get("name"));
        assertTrue(statement.setValues.containsKey("name"));
    }

    @Test
    public void testUpdate_NullValueUpperCase() {
        // Test UPDATE with NULL keyword (uppercase)
        PrepareResult result = StatementParser.prepareStatement("update courses set name = NULL", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNull(statement.setValues.get("name"));
        assertTrue(statement.setValues.containsKey("name"));
    }

    @Test
    public void testUpdate_MultipleColumnsWithNull() {
        // Test UPDATE with multiple columns, some null
        PrepareResult result = StatementParser.prepareStatement("update courses set name = null, code = CS101", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNull(statement.setValues.get("name"));
        assertEquals("CS101", statement.setValues.get("code"));
    }

    // ==================== DELETE Tests ====================

    @Test
    public void testDelete_All() {
        // Test DELETE without WHERE (deletes all rows)
        PrepareResult result = StatementParser.prepareStatement("delete from courses", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DELETE, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testDelete_WithWhere() {
        // Test DELETE with WHERE clause
        PrepareResult result = StatementParser.prepareStatement("delete from courses where id = 1", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testDelete_WithMultipleAndConditions() {
        // Test DELETE with multiple AND conditions
        PrepareResult result = StatementParser.prepareStatement("delete from courses where id = 1 and courseName = SE", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testDelete_WithMultipleOrConditions() {
        // Test DELETE with multiple OR conditions
        PrepareResult result = StatementParser.prepareStatement("delete from courses where status = old or status = deprecated", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testDelete_InvalidWhereCondition() {
        // Invalid WHERE condition
        PrepareResult result = StatementParser.prepareStatement("delete from courses where invalidcond", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    // ==================== Comparison Operator Tests ====================

    @Test
    public void testParseComp_Equals() {
        // Test = operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_GreaterThan() {
        // Test > operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id > 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_LessThan() {
        // Test < operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id < 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_GreaterEquals() {
        // Test >= operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id >= 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_LessEquals() {
        // Test <= operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id <= 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_NotEquals() {
        // Test != operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id != 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_Like() {
        // Test LIKE operator with pattern
        PrepareResult result = StatementParser.prepareStatement("select * from courses where courseName like 'Software%'", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_QuotedValue() {
        // Test comparison with quoted value containing spaces
        PrepareResult result = StatementParser.prepareStatement("select * from courses where courseName = 'Software Engineering Practice'", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_QuotedLikeValue() {
        // Test LIKE with quoted pattern
        PrepareResult result = StatementParser.prepareStatement("select * from courses where email like '%@example.com'", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_MultipleEqualSigns() {
        // Test value containing equals sign (should use first equals as operator)
        PrepareResult result = StatementParser.prepareStatement("select * from courses where email = 'a=b@test.com'", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_MultipleComparisonsInAnd() {
        // Test complex WHERE with multiple comparison types
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id > 0 and id < 100 and status = active", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertNotNull(statement.whereCondition);
    }

    @Test
    public void testParseComp_InvalidOperator() {
        // Unsupported operator
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id ~ 5", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testParseComp_MissingColumn() {
        // Operator at start of condition (no column name)
        PrepareResult result = StatementParser.prepareStatement("select * from courses where = 5", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testUnrecognizedStatement() {
        // Statement that doesn't match any pattern
        PrepareResult result = StatementParser.prepareStatement("invalid statement", statement);
        assertEquals(PrepareResult.UNRECOGNIZED_STATEMENT, result);
    }

    // ==================== LIMIT Tests ====================

    @Test
    public void testSelect_WithLimit() {
        // Test SELECT with LIMIT clause
        PrepareResult result = StatementParser.prepareStatement("select * from courses limit 10", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.SELECT, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals(10, statement.limit);
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testSelect_WithWhereAndLimit() {
        // Test SELECT with WHERE and LIMIT clauses
        PrepareResult result = StatementParser.prepareStatement("select * from courses where id > 5 limit 3", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.SELECT, statement.type);
        assertNotNull(statement.whereCondition);
        assertEquals(3, statement.limit);
    }

    @Test
    public void testSelect_WithColumnsAndLimit() {
        // Test SELECT with specific columns and LIMIT
        PrepareResult result = StatementParser.prepareStatement("select id, courseName from courses limit 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(2, statement.columns.size());
        assertEquals(5, statement.limit);
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testSelect_WithAllClausesAndLimit() {
        // Test SELECT with columns, WHERE, and LIMIT
        PrepareResult result = StatementParser.prepareStatement("select id, courseName from courses where id > 0 limit 20", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(2, statement.columns.size());
        assertNotNull(statement.whereCondition);
        assertEquals(20, statement.limit);
    }

    @Test
    public void testSelect_LimitNegative() {
        // Negative LIMIT not allowed
        PrepareResult result = StatementParser.prepareStatement("select * from courses limit -5", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testSelect_LimitInvalidNumber() {
        // Non-numeric LIMIT value
        PrepareResult result = StatementParser.prepareStatement("select * from courses limit abc", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_WithLimit() {
        // Test UPDATE with LIMIT clause
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName where id > 10 limit 5", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.UPDATE, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals("NewName", statement.setValues.get("courseName"));
        assertNotNull(statement.whereCondition);
        assertEquals(5, statement.limit);
    }

    @Test
    public void testUpdate_LimitNegative() {
        // Negative LIMIT not allowed for UPDATE
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName where id > 0 limit -1", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testUpdate_LimitInvalidNumber() {
        // Non-numeric LIMIT value for UPDATE
        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = NewName where id > 0 limit xyz", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testDelete_WithLimit() {
        // Test DELETE with LIMIT clause
        PrepareResult result = StatementParser.prepareStatement("delete from courses limit 10", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DELETE, statement.type);
        assertEquals("courses", statement.tableName);
        assertEquals(10, statement.limit);
        assertEquals(BooleanCondition.TRUE, statement.whereCondition);
    }

    @Test
    public void testDelete_WithWhereAndLimit() {
        // Test DELETE with WHERE and LIMIT clauses
        PrepareResult result = StatementParser.prepareStatement("delete from courses where status = old limit 3", statement);
        assertEquals(PrepareResult.SUCCESS, result);
        assertEquals(StatementType.DELETE, statement.type);
        assertNotNull(statement.whereCondition);
        assertEquals(3, statement.limit);
    }

    @Test
    public void testDelete_LimitNegative() {
        // Negative LIMIT not allowed for DELETE
        PrepareResult result = StatementParser.prepareStatement("delete from courses limit -5", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }

    @Test
    public void testDelete_LimitInvalidNumber() {
        // Non-numeric LIMIT value for DELETE
        PrepareResult result = StatementParser.prepareStatement("delete from courses limit bad", statement);
        assertEquals(PrepareResult.SYNTAX_ERROR, result);
    }
}
