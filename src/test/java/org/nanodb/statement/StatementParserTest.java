package org.nanodb.statement;

/**
 * Minimum test set for 100% Condition Coverage (CC), Decision Coverage (DC), and Branch Coverage (BC)
 */
public class StatementParserTest {
//    private Statement statement;
//
//    @BeforeEach
//    public void setUp() {
//        statement = new Statement();
//    }
//
//    // CREATE TABLE - 1 test (covers startsWith branch)
//    @Test
//    public void testCreateTable() {
//        PrepareResult result = StatementParser.prepareStatement("create table courses", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.CREATE_TABLE, statement.type);
//        assertEquals("courses", statement.tableName);
//    }
//
//    // DROP TABLE - 1 test (covers startsWith branch)
//    @Test
//    public void testDropTable() {
//        PrepareResult result = StatementParser.prepareStatement("drop table courses", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.DROP_TABLE, statement.type);
//        assertEquals("courses", statement.tableName);
//    }
//
//    // INSERT - 7 tests (covers: parts.length<4, id<0, courseName too long, courseCode too long, NumberFormatException, success)
//    @Test
//    public void testInsert_Success() {
//        PrepareResult result = StatementParser.prepareStatement("insert into courses 1 Software-Engineering-Practice CS3343", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.INSERT, statement.type);
//        assertEquals("courses", statement.tableName);
//        assertEquals(1, statement.rowToInsert.id);
//        assertEquals("Software-Engineering-Practice", statement.rowToInsert.courseName);
//        assertEquals("CS3343", statement.rowToInsert.courseCode);
//    }
//
//    @Test
//    public void testInsert_TooFewArguments() {
//        PrepareResult result = StatementParser.prepareStatement("insert into courses 1 Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SYNTAX_ERROR, result);
//    }
//
//    @Test
//    public void testInsert_NegativeId() {
//        PrepareResult result = StatementParser.prepareStatement("insert into courses -1 Software-Engineering-Practice CS3343", statement);
//        assertEquals(PrepareResult.NEGATIVE_ID, result);
//    }
//
//    @Test
//    public void testInsert_CourseNameTooLong() {
//        String longName = "a".repeat(Row.COURSE_NAME_SIZE + 1);
//        PrepareResult result = StatementParser.prepareStatement("insert into courses 1 " + longName + " CS3343", statement);
//        assertEquals(PrepareResult.STRING_TOO_LONG, result);
//    }
//
//    @Test
//    public void testInsert_CourseCodeTooLong() {
//        String longCode = "a".repeat(Row.COURSE_CODE_SIZE + 1);
//        PrepareResult result = StatementParser.prepareStatement("insert into courses 1 Software-Engineering-Practice " + longCode, statement);
//        assertEquals(PrepareResult.STRING_TOO_LONG, result);
//    }
//
//    @Test
//    public void testInsert_InvalidId() {
//        PrepareResult result = StatementParser.prepareStatement("insert into courses abc Software-Engineering-Practice CS3343", statement);
//        assertEquals(PrepareResult.SYNTAX_ERROR, result);
//    }
//
//    @Test
//    public void testInsert_ValidMaxLength() {
//        String maxName = "a".repeat(Row.COURSE_NAME_SIZE);
//        PrepareResult result = StatementParser.prepareStatement("insert into courses 1 " + maxName + " CS3343", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(maxName, statement.rowToInsert.courseName);
//    }
//
//    // SELECT - 6 tests (covers: fromIndex=-1, columns=*, columns!=*, whereParts.length>1, exception in parseWhere)
//    @Test
//    public void testSelect_AllColumns() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.SELECT, statement.type);
//        assertEquals("courses", statement.tableName);
//        assertNull(statement.columns);
//    }
//
//    @Test
//    public void testSelect_SpecificColumns() {
//        PrepareResult result = StatementParser.prepareStatement("select id, courseName from courses", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(2, statement.columns.size());
//        assertEquals("id", statement.columns.get(0));
//        assertEquals("courseName", statement.columns.get(1));
//    }
//
//    @Test
//    public void testSelect_MissingFrom() {
//        PrepareResult result = StatementParser.prepareStatement("select * courses", statement);
//        assertEquals(PrepareResult.SYNTAX_ERROR, result);
//    }
//
//    @Test
//    public void testSelect_WithWhere() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testSelect_WithAndCondition() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 and courseName = Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testSelect_WithOrCondition() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 1 or courseName = Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    // UPDATE - 5 tests (covers: setIndex=-1, kv.length!=2, quoted value, whereParts.length>1, exception)
//    @Test
//    public void testUpdate_Success() {
//        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.UPDATE, statement.type);
//        assertEquals("courses", statement.tableName);
//        assertEquals("Software-Engineering-Practice", statement.setValues.get("courseName"));
//    }
//
//    @Test
//    public void testUpdate_MissingSet() {
//        PrepareResult result = StatementParser.prepareStatement("update courses courseName = Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SYNTAX_ERROR, result);
//    }
//
//    @Test
//    public void testUpdate_InvalidFormat() {
//        PrepareResult result = StatementParser.prepareStatement("update courses set courseName Software-Engineering-Practice", statement);
//        assertEquals(PrepareResult.SYNTAX_ERROR, result);
//    }
//
//    @Test
//    public void testUpdate_QuotedValue() {
//        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = 'Software Engineering Practice'", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals("Software Engineering Practice", statement.setValues.get("courseName"));
//    }
//
//    @Test
//    public void testUpdate_WithWhere() {
//        PrepareResult result = StatementParser.prepareStatement("update courses set courseName = AI-for-Software-Engineering where id = 1", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    // DELETE - 2 tests (covers: whereParts.length>1, no where)
//    @Test
//    public void testDelete_All() {
//        PrepareResult result = StatementParser.prepareStatement("delete from courses", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertEquals(StatementType.DELETE, statement.type);
//        assertEquals("courses", statement.tableName);
//        assertNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testDelete_WithWhere() {
//        PrepareResult result = StatementParser.prepareStatement("delete from courses where id = 1", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    // parseComp - 4 tests (covers: opIndex=-1, = operator, > operator, < operator, quoted value)
//    @Test
//    public void testParseComp_Equals() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id = 5", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testParseComp_GreaterThan() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id > 5", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testParseComp_LessThan() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where id < 5", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    @Test
//    public void testParseComp_QuotedValue() {
//        PrepareResult result = StatementParser.prepareStatement("select * from courses where courseName = 'Software Engineering Practice'", statement);
//        assertEquals(PrepareResult.SUCCESS, result);
//        assertNotNull(statement.whereCondition);
//    }
//
//    // UNRECOGNIZED - 1 test (covers final return statement)
//    @Test
//    public void testUnrecognizedStatement() {
//        PrepareResult result = StatementParser.prepareStatement("invalid statement", statement);
//        assertEquals(PrepareResult.UNRECOGNIZED_STATEMENT, result);
//    }
}
