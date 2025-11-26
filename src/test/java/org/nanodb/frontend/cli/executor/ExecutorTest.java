package org.nanodb.frontend.cli.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.cli.executor.ExecuteResult;
import org.nanodb.cli.executor.Executor;
import org.nanodb.cli.statement.Statement;
import org.nanodb.cli.statement.StatementType;
import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;
import org.nanodb.internal.db.Database;
import org.nanodb.internal.db.Db;
import org.nanodb.internal.primitives.*;
import org.nanodb.internal.tableFactory.LinkedListTableFactory;
import org.nanodb.utils.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Executor
 * Tests all statement types: CREATE_TABLE, DROP_TABLE, INSERT, SELECT, UPDATE, DELETE
 * with various conditions including LIMIT functionality
 */
public class ExecutorTest {
    @TempDir
    Path tempDir;
    
    private Db db;
    private Statement statement;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private FileChunker chunker;
    
    @BeforeEach
    public void setUp() throws IOException {
        chunker = new FileChunker(tempDir.resolve("test.db").toString(), 4096, 1000);
        LinkedListTableFactory factory = new LinkedListTableFactory(chunker);
        db = new Database(factory);
        statement = new Statement();
        
        // Capture System.out
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        if (chunker != null) {
            chunker.close();
        }
    }
    
    // ==================== CREATE TABLE ====================
    
    @Test
    public void testCreateTable_Success() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.CREATE_TABLE;
        statement.tableName = "courses";
        
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("id", new Int32Type()));
        fields.add(new Pair<>("name", new VarCharType(50)));
        statement.tableSchema = new StructType(fields);
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(db.tableExists("courses"));
    }
    
    @Test
    public void testCreateTable_Duplicate() throws IOException, MissingWhereConditionException {
        // Create table first time
        statement.type = StatementType.CREATE_TABLE;
        statement.tableName = "courses";
        
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("id", new Int32Type()));
        statement.tableSchema = new StructType(fields);
        
        Executor.execute(statement, db);
        
        // Try to create again
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.ERROR, result);
    }
    
    // ==================== DROP TABLE ====================
    
    @Test
    public void testDropTable_TableNotFound() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.DROP_TABLE;
        statement.tableName = "nonexistent";
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
    }
    
    @Test
    public void testDropTable_Success() throws IOException, MissingWhereConditionException {
        // Create table first
        createTestTable("courses");
        
        // Drop it
        statement.type = StatementType.DROP_TABLE;
        statement.tableName = "courses";
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        assertFalse(db.tableExists("courses"));
    }
    
    // ==================== INSERT ====================
    
    @Test
    public void testInsert_TableNotFound() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.INSERT;
        statement.tableName = "nonexistent";
        statement.valuesToInsert.put("id", "1");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
    }
    
    @Test
    public void testInsert_Success() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        
        statement.type = StatementType.INSERT;
        statement.tableName = "courses";
        statement.valuesToInsert.put("id", "1");
        statement.valuesToInsert.put("name", "Software Engineering");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Verify insertion
        List<Map<String, Object>> rows = db.getTable("courses").selectAll();
        assertEquals(1, rows.size());
        assertEquals(1, rows.get(0).get("id"));
    }
    
    @Test
    public void testInsert_DataTypeMismatch() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        
        statement.type = StatementType.INSERT;
        statement.tableName = "courses";
        statement.valuesToInsert.put("id", "not_a_number");
        statement.valuesToInsert.put("name", "Test");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, result);
    }
    
    // ==================== SELECT ====================
    
    @Test
    public void testSelect_TableNotFound() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.SELECT;
        statement.tableName = "nonexistent";
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
    }
    
    @Test
    public void testSelect_AllColumns() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = null;
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(outputStream.toString().contains("1"));
        assertTrue(outputStream.toString().contains("SE"));
    }
    
    @Test
    public void testSelect_SpecificColumns() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = Arrays.asList("id");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        assertTrue(outputStream.toString().contains("1"));
    }
    
    @Test
    public void testSelect_WithCondition() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        String output = outputStream.toString();
        assertTrue(output.contains("SE"));
        assertFalse(output.contains("AI"));
    }
    
    @Test
    public void testSelect_WithLimit() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        insertTestData("courses", 3, "ML");
        
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.limit = 2;
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Should only show 2 results
        String output = outputStream.toString();
        int lineCount = output.split("\n").length;
        assertEquals(2, lineCount);
    }
    
    @Test
    public void testSelect_InvalidColumn() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        
        statement.type = StatementType.SELECT;
        statement.tableName = "courses";
        statement.columns = Arrays.asList("invalidColumn");
        
        assertThrows(RuntimeException.class, () -> Executor.execute(statement, db));
    }
    
    // ==================== UPDATE ====================
    
    @Test
    public void testUpdate_TableNotFound() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.UPDATE;
        statement.tableName = "nonexistent";
        statement.setValues.put("name", "New Name");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
    }
    
    @Test
    public void testUpdate_Success() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("name", "Updated");
        statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Verify update
        List<Map<String, Object>> rows = db.getTable("courses").selectAll();
        assertEquals("Updated", rows.get(0).get("name"));
        assertTrue(outputStream.toString().contains("Updated 1 row(s)"));
    }
    
    @Test
    public void testUpdate_WithLimit() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        insertTestData("courses", 3, "ML");
        
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("name", "Updated");
        statement.limit = 2;
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Should update only 2 rows
        assertTrue(outputStream.toString().contains("Updated 2 row(s)"));
    }
    
    @Test
    public void testUpdate_DataTypeMismatch() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        
        statement.type = StatementType.UPDATE;
        statement.tableName = "courses";
        statement.setValues.put("id", "not_a_number");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, result);
    }
    
    // ==================== DELETE ====================
    
    @Test
    public void testDelete_TableNotFound() throws IOException, MissingWhereConditionException {
        statement.type = StatementType.DELETE;
        statement.tableName = "nonexistent";
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.TABLE_NOT_FOUND, result);
    }
    
    @Test
    public void testDelete_Success() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Verify deletion
        List<Map<String, Object>> rows = db.getTable("courses").selectAll();
        assertEquals(1, rows.size());
        assertEquals(2, rows.get(0).get("id"));
        assertTrue(outputStream.toString().contains("Deleted 1 row(s)"));
    }
    
    @Test
    public void testDelete_WithLimit() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        insertTestData("courses", 3, "ML");
        
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        statement.limit = 2;
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Should delete only 2 rows
        List<Map<String, Object>> rows = db.getTable("courses").selectAll();
        assertEquals(1, rows.size());
        assertTrue(outputStream.toString().contains("Deleted 2 row(s)"));
    }
    
    @Test
    public void testDelete_All() throws IOException, MissingWhereConditionException {
        createTestTable("courses");
        insertTestData("courses", 1, "SE");
        insertTestData("courses", 2, "AI");
        
        statement.type = StatementType.DELETE;
        statement.tableName = "courses";
        
        ExecuteResult result = Executor.execute(statement, db);
        assertEquals(ExecuteResult.SUCCESS, result);
        
        // Verify all deleted
        List<Map<String, Object>> rows = db.getTable("courses").selectAll();
        assertEquals(0, rows.size());
    }
    
    // ==================== Helper Methods ====================
    
    private void createTestTable(String tableName) throws IOException, MissingWhereConditionException {
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("id", new Int32Type()));
        fields.add(new Pair<>("name", new VarCharType(50)));
        StructType schema = new StructType(fields);
        
        db.createTable(tableName, schema);
    }
    
    private void insertTestData(String tableName, int id, String name) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("name", name);
        db.getTable(tableName).insert(row);
    }
}

