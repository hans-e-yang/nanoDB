package org.nanodb.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.db.Db;
import org.nanodb.db.Row;
import org.nanodb.statement.Statement;
import org.nanodb.statement.StatementType;
import org.nanodb.statement.condition.Condition;
import org.nanodb.statement.condition.ComparisonCondition;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutorTest {
    // ==================== STUBDB CLASS ====================
    // Stub Class for Db

    private static class StubDb extends Db {
        private boolean createTableResult;
        private boolean dropTableResult;
        private boolean insertResult;
        private List<Row> selectResult;
        private int updateResult;
        private int deleteResult;
        private boolean tableExists;

        private boolean wasCreateTableCalled = false;
        private boolean wasDropTableCalled = false;
        private boolean wasInsertCalled = false;
        private boolean wasSelectCalled = false;
        private boolean wasUpdateCalled = false;
        private boolean wasDeleteCalled = false;

        private String lastCreatedTableName;
        private String lastDroppedTableName;
        private String lastInsertTableName;
        private Row lastInsertedRow;
        private String lastSelectTableName;
        private Condition lastSelectCondition;
        private String lastUpdateTableName;
        private Map<String, String> lastUpdateSetValues;
        private Condition lastUpdateCondition;
        private String lastDeleteTableName;
        private Condition lastDeleteCondition;

        private int insertCallCount = 0;

        public StubDb() {
            super(null);
            this.selectResult = new ArrayList<>();
        }

        @Override
        public boolean createTable(String tableName) {
            wasCreateTableCalled = true;
            lastCreatedTableName = tableName;
            return createTableResult;
        }

        @Override
        public boolean dropTable(String tableName) {
            wasDropTableCalled = true;
            lastDroppedTableName = tableName;
            return dropTableResult;
        }

        @Override
        public boolean insert(String tableName, Row row) {
            wasInsertCalled = true;
            lastInsertTableName = tableName;
            lastInsertedRow = row;
            insertCallCount++;
            return insertResult;
        }

        @Override
        public List<Row> select(String tableName, Condition condition) {
            wasSelectCalled = true;
            lastSelectTableName = tableName;
            lastSelectCondition = condition;
            return selectResult;
        }

        @Override
        public int delete(String tableName, Condition condition) {
            wasDeleteCalled = true;
            lastDeleteTableName = tableName;
            lastDeleteCondition = condition;
            return deleteResult;
        }

        @Override
        public int update(String tableName, Map<String, String> setValues, Condition condition) {
            wasUpdateCalled = true;
            lastUpdateTableName = tableName;
            lastUpdateSetValues = setValues;
            lastUpdateCondition = condition;
            return updateResult;
        }

        @Override
        public boolean tableExists(String tableName) {
            return tableExists;
        }

        // Setters for test control
        public void setCreateTableResult(boolean result) { this.createTableResult = result; }
        public void setDropTableResult(boolean result) { this.dropTableResult = result; }
        public void setInsertResult(boolean result) { this.insertResult = result; }
        public void setSelectResult(List<Row> result) { this.selectResult = result; }
        public void setUpdateResult(int result) { this.updateResult = result; }
        public void setDeleteResult(int result) { this.deleteResult = result; }
        public void setTableExists(boolean exists) { this.tableExists = exists; }

        // Getters for test verification
        public boolean wasCreateTableCalled() { return wasCreateTableCalled; }
        public boolean wasDropTableCalled() { return wasDropTableCalled; }
        public boolean wasInsertCalled() { return wasInsertCalled; }
        public boolean wasSelectCalled() { return wasSelectCalled; }
        public boolean wasUpdateCalled() { return wasUpdateCalled; }
        public boolean wasDeleteCalled() { return wasDeleteCalled; }
        public String getLastCreatedTableName() { return lastCreatedTableName; }
        public String getLastDroppedTableName() { return lastDroppedTableName; }
        public String getLastInsertTableName() { return lastInsertTableName; }
        public Row getLastInsertedRow() { return lastInsertedRow; }
        public String getLastSelectTableName() { return lastSelectTableName; }
        public Condition getLastSelectCondition() { return lastSelectCondition; }
        public List<Row> getLastSelectResult() { return selectResult; }
        public String getLastUpdateTableName() { return lastUpdateTableName; }
        public Map<String, String> getLastUpdateSetValues() { return lastUpdateSetValues; }
        public Condition getLastUpdateCondition() { return lastUpdateCondition; }
        public String getLastDeleteTableName() { return lastDeleteTableName; }
        public Condition getLastDeleteCondition() { return lastDeleteCondition; }
        public int getLastDeleteResult() { return deleteResult; }
        public int getInsertCallCount() { return insertCallCount; }
    }

    private Statement statement;
    private StubDb StubDb;

    @BeforeEach
    public void setUp() {
        statement = new Statement();
        StubDb = new StubDb();
    }

    // ==================== CREATE TABLE TESTS ====================
    // Tests for CREATE TABLE statement execution

    @Test
    public void testCreateTableSuccess() throws IOException {
        statement.type = StatementType.CREATE_TABLE;
        statement.tableName = "courses";
        StubDb.setCreateTableResult(true);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasCreateTableCalled());
        assertEquals("courses", StubDb.getLastCreatedTableName());
    }

    @Test
    public void testCreateTableFailure() throws IOException {
        statement.type = StatementType.CREATE_TABLE;
        statement.tableName = "courses";
        StubDb.setCreateTableResult(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.ERROR, result);
        assertTrue(StubDb.wasCreateTableCalled());
    }

    @Test
    public void testCreateTableWithDifferentNames() throws IOException {
        statement.type = StatementType.CREATE_TABLE;
        statement.tableName = "students";
        StubDb.setCreateTableResult(true);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals("students", StubDb.getLastCreatedTableName());
    }

    // ==================== DROP TABLE TESTS ====================
    // Tests for DROP TABLE statement execution

    @Test
    public void testDropTableSuccess() throws IOException {
        statement.type = StatementType.DROP_TABLE;
        statement.tableName = "courses";
        StubDb.setTableExists(true);
        StubDb.setDropTableResult(true);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasDropTableCalled());
        assertEquals("courses", StubDb.getLastDroppedTableName());
    }

    @Test
    public void testDropTableNotFound() throws IOException {
        statement.type = StatementType.DROP_TABLE;
        statement.tableName = "nonexistent";
        StubDb.setTableExists(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
        assertFalse(StubDb.wasDropTableCalled());
    }

    @Test
    public void testDropTableError() throws IOException {
        statement.type = StatementType.DROP_TABLE;
        statement.tableName = "courses";
        StubDb.setTableExists(true);
        StubDb.setDropTableResult(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.ERROR, result);
        assertTrue(StubDb.wasDropTableCalled());
    }

    // ==================== INSERT TESTS ====================
    // Tests for INSERT statement execution

    @Test
    public void testInsertSuccess() throws IOException {
        statement.type = StatementType.INSERT;
        statement.tableName = "courses";
        statement.rowToInsert = new Row(1, "Software-Engineering-Practice", "CS3343");
        StubDb.setTableExists(true);
        StubDb.setInsertResult(true);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasInsertCalled());
        assertEquals("courses", StubDb.getLastInsertTableName());
        assertEquals(statement.rowToInsert, StubDb.getLastInsertedRow());
    }

    @Test
    public void testInsertTableNotFound() throws IOException {
        statement.type = StatementType.INSERT;
        statement.tableName = "nonexistent";
        statement.rowToInsert = new Row(1, "Software-Engineering-Practice", "CS3343");
        StubDb.setTableExists(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
        assertFalse(StubDb.wasInsertCalled());
    }

    @Test
    public void testInsertDuplicateKey() throws IOException {
        statement.type = StatementType.INSERT;
        statement.tableName = "courses";
        statement.rowToInsert = new Row(1, "Software-Engineering-Practice", "CS3343");
        StubDb.setTableExists(true);
        StubDb.setInsertResult(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.DUPLICATE_KEY, result);
        assertTrue(StubDb.wasInsertCalled());
    }

    @Test
    public void testInsertMultipleRows() throws IOException {
        statement.type = StatementType.INSERT;
        statement.tableName = "courses";
        StubDb.setTableExists(true);
        StubDb.setInsertResult(true);

        // Insert first row
        statement.rowToInsert = new Row(1, "Software-Engineering-Practice", "CS3343");
        ExecuteResult result1 = Executor.execute(statement, StubDb);

        // Insert second row
        statement.rowToInsert = new Row(2, "Computer-Programming", "CS2310");
        ExecuteResult result2 = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result1);
        assertEquals(ExecuteResult.SUCCESS, result2);
        assertEquals(2, StubDb.getInsertCallCount());
    }

    // ==================== SELECT TESTS ====================
    // Tests for SELECT statement execution

    @Test
    public void testSelectAllColumnsSuccess() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null; // All columns
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
        assertEquals("courses", StubDb.getLastSelectTableName());
        assertEquals(expectedRows, StubDb.getLastSelectResult());
    }

    @Test
    public void testSelectSpecificColumnsSuccess() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = Arrays.asList("id", "courseName");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectSingleColumnSuccess() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = Arrays.asList("courseName");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectEmptyColumnsListSuccess() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectWithWhereCondition() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        statement.whereCondition = new ComparisonCondition("id", "=", "1");
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
        assertEquals(statement.whereCondition, StubDb.getLastSelectCondition());
    }

    @Test
    public void testSelectTableNotFound() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "nonexistent";
        statement.columns = null;
        StubDb.setTableExists(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
        assertFalse(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectEmptyResult() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setSelectResult(new ArrayList<>());

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
        assertTrue(StubDb.getLastSelectResult().isEmpty());
    }

    @Test
    public void testSelectMultipleRows() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        expectedRows.add(new Row(2, "Computer-Programming", "CS2310"));
        expectedRows.add(new Row(3, "Algorithms", "CS4335"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(3, StubDb.getLastSelectResult().size());
    }

    // ==================== UPDATE TESTS ====================
    // Tests for UPDATE statement execution

    @Test
    public void testUpdateSuccess() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("courseName", "Computer-Programming");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setUpdateResult(1);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasUpdateCalled());
        assertEquals("courses", StubDb.getLastUpdateTableName());
        assertEquals(statement.setValues, StubDb.getLastUpdateSetValues());
    }

    @Test
    public void testUpdateTableNotFound() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "nonexistent";
        statement.setValues.put("courseName", "Computer-Programming");
        StubDb.setTableExists(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
        assertFalse(StubDb.wasUpdateCalled());
    }

    @Test
    public void testUpdateMultipleRows() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("courseName", "Computer-Programming");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setUpdateResult(3);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasUpdateCalled());
    }

    @Test
    public void testUpdateZeroRows() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("courseName", "Computer-Programming");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setUpdateResult(0);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasUpdateCalled());
    }

    @Test
    public void testUpdateMultipleColumns() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("courseName", "Computer-Programming");
        statement.setValues.put("courseCode", "CS2310");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setUpdateResult(1);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(2, StubDb.getLastUpdateSetValues().size());
    }

    @Test
    public void testUpdateWithWhereCondition() throws IOException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("courseName", "Computer-Programming");
        statement.whereCondition = new ComparisonCondition("id", "=", "1");
        StubDb.setTableExists(true);
        StubDb.setUpdateResult(1);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasUpdateCalled());
        assertEquals(statement.whereCondition, StubDb.getLastUpdateCondition());
    }

    // ==================== DELETE TESTS ====================
    // Tests for DELETE statement execution

    @Test
    public void testDeleteSuccess() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setDeleteResult(1);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasDeleteCalled());
        assertEquals("courses", StubDb.getLastDeleteTableName());
    }

    @Test
    public void testDeleteTableNotFound() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "nonexistent";
        statement.whereCondition = null;
        StubDb.setTableExists(false);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
        assertFalse(StubDb.wasDeleteCalled());
    }

    @Test
    public void testDeleteMultipleRows() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setDeleteResult(3);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasDeleteCalled());
    }

    @Test
    public void testDeleteZeroRows() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setDeleteResult(0);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasDeleteCalled());
    }

    @Test
    public void testDeleteWithWhereCondition() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = new ComparisonCondition("id", "=", "1");
        StubDb.setTableExists(true);
        StubDb.setDeleteResult(1);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasDeleteCalled());
        assertEquals(statement.whereCondition, StubDb.getLastDeleteCondition());
    }

    @Test
    public void testDeleteAll() throws IOException {
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        StubDb.setDeleteResult(10);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(10, StubDb.getLastDeleteResult());
    }

    // ==================== FORMATROW TESTS ====================
    // Tests for formatRow method - covers all column selection branches

    @Test
    public void testSelectFormatRowAllColumns() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowEmptyColumnsAliasAllColumns() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(2, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowIdColumn() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("id");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(5, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowCourseNameColumn() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("courseName");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(3, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowCourseCodeColumn() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("courseCode");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(4, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowTwoColumnsWithComma() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("id");
        statement.columns.add("courseName");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowThreeColumnsWithCommas() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("id");
        statement.columns.add("courseName");
        statement.columns.add("courseCode");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(6, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowInvalidColumnThrowsException() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("invalidColumn");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        assertThrows(RuntimeException.class, () -> {
            Executor.execute(statement, StubDb);
        });
    }

    @Test
    public void testSelectFormatRowSingleRowIdAndCourseCode() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("id");
        statement.columns.add("courseCode");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(10, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectFormatRowCourseNameAndCourseCode() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("courseName");
        statement.columns.add("courseCode");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(7, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(StubDb.wasSelectCalled());
    }

    @Test
    public void testSelectMultipleRowsAllColumnsFormatting() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        expectedRows.add(new Row(2, "Computer-Programming", "CS2310"));
        expectedRows.add(new Row(3, "Algorithms", "CS4335"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(3, StubDb.getLastSelectResult().size());
    }

    @Test
    public void testSelectMultipleRowsSpecificColumnsFormatting() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("courseName");
        statement.columns.add("id");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        expectedRows.add(new Row(2, "Computer-Programming", "CS2310"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(2, StubDb.getLastSelectResult().size());
    }

    @Test
    public void testSelectSingleRowSingleColumnFormatting() throws IOException {
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = new ArrayList<>();
        statement.columns.add("courseCode");
        statement.whereCondition = null;
        StubDb.setTableExists(true);
        List<Row> expectedRows = new ArrayList<>();
        expectedRows.add(new Row(1, "Software-Engineering-Practice", "CS3343"));
        StubDb.setSelectResult(expectedRows);

        ExecuteResult result = Executor.execute(statement, StubDb);

        assertEquals(ExecuteResult.SUCCESS, result);
        assertEquals(1, StubDb.getLastSelectResult().size());
    }
}
