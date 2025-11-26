package org.nanodb.frontend.cli.executor;

import org.junit.jupiter.api.*;
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
 * Test suite for CLI Executor.
 * Tests CREATE_TABLE, DROP_TABLE, INSERT, SELECT, UPDATE, DELETE operations
 * with Int32, VarChar, and Date types.
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

		outputStream = new ByteArrayOutputStream();
		originalOut = System.out;
		System.setOut(new PrintStream(outputStream));
	}

	@AfterEach
	public void tearDown() throws IOException {
		System.setOut(originalOut);
		if (chunker != null) {
			chunker.close();
		}
	}

	// ==================== Helper Methods ====================

	private void createBasicTable(String tableName) throws IOException, MissingWhereConditionException {
		List<Pair<String, DataType<?>>> fields = new ArrayList<>();
		fields.add(new Pair<>("id", new Int32Type()));
		fields.add(new Pair<>("name", new VarCharType(50)));
		db.createTable(tableName, new StructType(fields));
	}

	private void createFullTable(String tableName) throws IOException, MissingWhereConditionException {
		List<Pair<String, DataType<?>>> fields = new ArrayList<>();
		fields.add(new Pair<>("id", new Int32Type()));
		fields.add(new Pair<>("name", new VarCharType(50)));
		fields.add(new Pair<>("date", new DateType()));
		db.createTable(tableName, new StructType(fields));
	}

	private void insertRow(String tableName, int id, String name) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("name", name);
		db.getTable(tableName).insert(row);
	}

	private void insertRow(String tableName, int id, String name, Date date) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("name", name);
		row.put("date", date);
		db.getTable(tableName).insert(row);
	}

	// ==================== CREATE TABLE ====================

	@Nested
	@DisplayName("CREATE TABLE")
	class CreateTableTests {

		@Test
		@DisplayName("Success - creates new table")
		void testSuccess() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.CREATE_TABLE;
			statement.tableName = "test";
			statement.tableSchema = new StructType(List.of(
				new Pair<>("id", new Int32Type()),
				new Pair<>("name", new VarCharType(50))
			));

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(db.tableExists("test"));
		}

		@Test
		@DisplayName("Error - duplicate table")
		void testDuplicate() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.CREATE_TABLE;
			statement.tableName = "test";
			statement.tableSchema = new StructType(List.of(new Pair<>("id", new Int32Type())));

			assertEquals(ExecuteResult.ERROR, Executor.execute(statement, db));
		}
	}

	// ==================== DROP TABLE ====================

	@Nested
	@DisplayName("DROP TABLE")
	class DropTableTests {

		@Test
		@DisplayName("Success - drops existing table")
		void testSuccess() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.DROP_TABLE;
			statement.tableName = "test";

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertFalse(db.tableExists("test"));
		}

		@Test
		@DisplayName("Error - table not found")
		void testNotFound() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.DROP_TABLE;
			statement.tableName = "nonexistent";

			assertEquals(ExecuteResult.TABLE_NOT_FOUND, Executor.execute(statement, db));
		}
	}

	// ==================== INSERT ====================

	@Nested
	@DisplayName("INSERT")
	class InsertTests {

		@Test
		@DisplayName("Success - inserts all columns")
		void testSuccess() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			statement.valuesToInsert.put("name", "Alice");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));

			List<Map<String, Object>> rows = db.getTable("test").selectAll();
			assertEquals(1, rows.size());
			assertEquals(1, rows.get(0).get("id"));
			assertEquals("Alice", rows.get(0).get("name"));
		}

		@Test
		@DisplayName("Success - partial columns use defaults")
		void testPartialColumns() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			// name not provided

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));

			List<Map<String, Object>> rows = db.getTable("test").selectAll();
			assertEquals("", rows.get(0).get("name")); // default for varchar
		}

		@Test
		@DisplayName("Success - null value converts to default")
		void testNullValue() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			statement.valuesToInsert.put("name", null);

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));

			List<Map<String, Object>> rows = db.getTable("test").selectAll();
			assertEquals("", rows.get(0).get("name"));
		}

		@Test
		@DisplayName("Success - with DateType")
		void testWithDateType() throws IOException, MissingWhereConditionException {
			createFullTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			statement.valuesToInsert.put("name", "Event");
			statement.valuesToInsert.put("date", "2025-11-26");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));

			List<Map<String, Object>> rows = db.getTable("test").selectAll();
			assertTrue(rows.get(0).get("date") instanceof Date);
		}

		@Test
		@DisplayName("Success - null date converts to epoch")
		void testNullDate() throws IOException, MissingWhereConditionException {
			createFullTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			statement.valuesToInsert.put("name", "Event");
			statement.valuesToInsert.put("date", null);

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));

			List<Map<String, Object>> rows = db.getTable("test").selectAll();
			assertEquals(new Date(0), rows.get(0).get("date"));
		}

		@Test
		@DisplayName("Error - table not found")
		void testNotFound() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.INSERT;
			statement.tableName = "nonexistent";
			statement.valuesToInsert.put("id", "1");

			assertEquals(ExecuteResult.TABLE_NOT_FOUND, Executor.execute(statement, db));
		}

		@Test
		@DisplayName("Error - int type mismatch")
		void testIntTypeMismatch() throws IOException, MissingWhereConditionException {
			createBasicTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "not_a_number");
			statement.valuesToInsert.put("name", "Test");

			assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, Executor.execute(statement, db));
		}

		@Test
		@DisplayName("Error - date type mismatch")
		void testDateTypeMismatch() throws IOException, MissingWhereConditionException {
			createFullTable("test");

			statement.type = StatementType.INSERT;
			statement.tableName = "test";
			statement.valuesToInsert.put("id", "1");
			statement.valuesToInsert.put("name", "Event");
			statement.valuesToInsert.put("date", "invalid-date");

			assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, Executor.execute(statement, db));
		}
	}

	// ==================== SELECT ====================

	@Nested
	@DisplayName("SELECT")
	class SelectTests {

		@Test
		@DisplayName("Success - all columns")
		void testAllColumns() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "Alice");

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.columns = null;

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(outputStream.toString().contains("Alice"));
		}

		@Test
		@DisplayName("Success - specific columns")
		void testSpecificColumns() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "Alice");

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.columns = List.of("id");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(outputStream.toString().contains("1"));
		}

		@Test
		@DisplayName("Success - with WHERE condition")
		void testWithCondition() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "Alice");
			insertRow("test", 2, "Bob");

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(outputStream.toString().contains("Alice"));
			assertFalse(outputStream.toString().contains("Bob"));
		}

		@Test
		@DisplayName("Success - with LIMIT")
		void testWithLimit() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");
			insertRow("test", 2, "B");
			insertRow("test", 3, "C");

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.limit = 2;

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertEquals(2, outputStream.toString().split("\n").length);
		}

		@Test
		@DisplayName("Success - with date condition")
		void testDateCondition() throws IOException, MissingWhereConditionException {
			createFullTable("test");
			insertRow("test", 1, "Old", new Date(1000000000000L)); // 2001
			insertRow("test", 2, "New", new Date(1700000000000L)); // 2023

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.whereCondition = new ComparisonCondition("date", ComparisonOperation.GREATER_THAN, "2020-01-01");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(outputStream.toString().contains("New"));
		}

		@Test
		@DisplayName("Error - table not found")
		void testNotFound() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.SELECT;
			statement.tableName = "nonexistent";

			assertEquals(ExecuteResult.TABLE_NOT_FOUND, Executor.execute(statement, db));
		}

		@Test
		@DisplayName("Error - invalid column")
		void testInvalidColumn() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "Alice");

			statement.type = StatementType.SELECT;
			statement.tableName = "test";
			statement.columns = List.of("invalid");

			assertThrows(RuntimeException.class, () -> Executor.execute(statement, db));
		}
	}

	// ==================== UPDATE ====================

	@Nested
	@DisplayName("UPDATE")
	class UpdateTests {

		@Test
		@DisplayName("Success - updates matching rows")
		void testSuccess() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "Old");

			statement.type = StatementType.UPDATE;
			statement.tableName = "test";
			statement.setValues.put("name", "New");
			statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertEquals("New", db.getTable("test").selectAll().get(0).get("name"));
			assertTrue(outputStream.toString().contains("Updated 1 row(s)"));
		}

		@Test
		@DisplayName("Success - with LIMIT")
		void testWithLimit() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");
			insertRow("test", 2, "B");
			insertRow("test", 3, "C");

			statement.type = StatementType.UPDATE;
			statement.tableName = "test";
			statement.setValues.put("name", "Updated");
			statement.limit = 2;

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(outputStream.toString().contains("Updated 2 row(s)"));
		}

		@Test
		@DisplayName("Success - with DateType")
		void testDateType() throws IOException, MissingWhereConditionException {
			createFullTable("test");
			insertRow("test", 1, "Event", new Date(0));

			statement.type = StatementType.UPDATE;
			statement.tableName = "test";
			statement.setValues.put("date", "2025-12-25");
			statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertTrue(db.getTable("test").selectAll().get(0).get("date") instanceof Date);
		}

		@Test
		@DisplayName("Error - table not found")
		void testNotFound() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.UPDATE;
			statement.tableName = "nonexistent";
			statement.setValues.put("name", "Test");

			assertEquals(ExecuteResult.TABLE_NOT_FOUND, Executor.execute(statement, db));
		}

		@Test
		@DisplayName("Error - int type mismatch")
		void testIntTypeMismatch() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");

			statement.type = StatementType.UPDATE;
			statement.tableName = "test";
			statement.setValues.put("id", "not_a_number");

			assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, Executor.execute(statement, db));
		}

		@Test
		@DisplayName("Error - date type mismatch")
		void testDateTypeMismatch() throws IOException, MissingWhereConditionException {
			createFullTable("test");
			insertRow("test", 1, "Event", new Date(0));

			statement.type = StatementType.UPDATE;
			statement.tableName = "test";
			statement.setValues.put("date", "invalid-date");

			assertEquals(ExecuteResult.DATA_TYPE_MISMATCH, Executor.execute(statement, db));
		}
	}

	// ==================== DELETE ====================

	@Nested
	@DisplayName("DELETE")
	class DeleteTests {

		@Test
		@DisplayName("Success - deletes matching rows")
		void testSuccess() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");
			insertRow("test", 2, "B");

			statement.type = StatementType.DELETE;
			statement.tableName = "test";
			statement.whereCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, "1");

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertEquals(1, db.getTable("test").selectAll().size());
			assertTrue(outputStream.toString().contains("Deleted 1 row(s)"));
		}

		@Test
		@DisplayName("Success - with LIMIT")
		void testWithLimit() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");
			insertRow("test", 2, "B");
			insertRow("test", 3, "C");

			statement.type = StatementType.DELETE;
			statement.tableName = "test";
			statement.limit = 2;

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertEquals(1, db.getTable("test").selectAll().size());
			assertTrue(outputStream.toString().contains("Deleted 2 row(s)"));
		}

		@Test
		@DisplayName("Success - deletes all without condition")
		void testDeleteAll() throws IOException, MissingWhereConditionException {
			createBasicTable("test");
			insertRow("test", 1, "A");
			insertRow("test", 2, "B");

			statement.type = StatementType.DELETE;
			statement.tableName = "test";

			assertEquals(ExecuteResult.SUCCESS, Executor.execute(statement, db));
			assertEquals(0, db.getTable("test").selectAll().size());
		}

		@Test
		@DisplayName("Error - table not found")
		void testNotFound() throws IOException, MissingWhereConditionException {
			statement.type = StatementType.DELETE;
			statement.tableName = "nonexistent";

			assertEquals(ExecuteResult.TABLE_NOT_FOUND, Executor.execute(statement, db));
		}
	}
}

