package org.nanodb.orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.orm.annotations.Ignore;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.internal.db.Database;
import java.util.List;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NanoDb} covering automatic migrations, ignore annotation handling
 * and validation of model inheritance requirements.
 */
public class NanoDbTest {

	// Simple model with two fields
	public static class User extends BaseModel { public int id; public String name; }
	// Modified schema version of User adds an extra field
	public static class UserV2 extends BaseModel { public int id; public String name; public int age; }

	// NanoDb subclass with one table and an ignored field
	static class TestDb extends NanoDb {
		public Table<User> users; // Should be created
		@Ignore public String transientValue; // Should be ignored
		public TestDb(String filename) throws IOException, IllegalAccessException { super(filename); }
	}

	// Second version referencing updated model schema
	static class TestDbV2 extends NanoDb {
		public Table<UserV2> users; // Should trigger migration if schema changed
		public TestDbV2(String filename) throws IOException, IllegalAccessException { super(filename); }
	}

	@Test
	@DisplayName("Creates tables from model fields and ignores @Ignore annotated fields")
	void createsTablesAndIgnoresAnnotated(@TempDir Path tempDir) throws Exception {
		String dbFile = tempDir.resolve("nano-test.db").toString();
		try (TestDb db = new TestDb(dbFile)) {
			assertNotNull(db.users, "users table should be initialized");

			// The ignored field should remain null (never set via reflection)
			Field ignoredField = TestDb.class.getDeclaredField("transientValue");
			ignoredField.setAccessible(true);
			assertNull(ignoredField.get(db));
		}
	}

	@Test
	@DisplayName("Migrates schema when model structure changes between instantiations")
	void migratesSchemaOnChange(@TempDir Path tempDir) throws Exception {
		String dbFile = tempDir.resolve("nano-migrate.db").toString();
		StructType originalSchema;
		// First version
		try (TestDb first = new TestDb(dbFile)) {
			assertNotNull(first.users);
			// Capture original schema
			originalSchema = getTableSchema(first.users);
		}

		// Second version with extra field triggers recreation
		try (TestDbV2 second = new TestDbV2(dbFile)) {
			StructType newSchema = getTableSchema(second.users);

			// Schemas should differ in field count and names
			assertNotEquals(originalSchema.getFields().size(), newSchema.getFields().size());
			assertTrue(newSchema.getFields().stream().anyMatch(p -> p.getKey().equals("age")));
		}
	}

	// Additional models for extended coverage
	public static class Order extends BaseModel { public int id; public String status; }
	static class TestDbTwoTables extends NanoDb {
		public Table<User> users;
		public Table<Order> orders;
		// Non-table field should be ignored even without @Ignore
		public int someCounter = 0;
		public TestDbTwoTables(String f) throws IOException, IllegalAccessException { super(f); }
	}
	static class TestDbOneTable extends NanoDb {
		public Table<User> users; // orders removed
		public TestDbOneTable(String f) throws IOException, IllegalAccessException { super(f); }
	}

	@Test
	@DisplayName("Drops tables no longer declared in subclass on reinitialization")
	void dropsRemovedTables(@TempDir Path tempDir) throws Exception {
		String file = tempDir.resolve("nano-drop.db").toString();
		try (TestDbTwoTables first = new TestDbTwoTables(file)) {
			assertNotNull(first.users);
			assertNotNull(first.orders);
			// Insert a user row then reinstantiate with one-table version
			User u = new User(); u.id = 7; u.name = "X"; first.users.insert(u);

			// Reflect internal db for table existence checks
			Database dbBefore = getInternalDatabase(first);
			assertTrue(dbBefore.getAllTables().contains("orders"));
			assertTrue(dbBefore.getAllTables().contains("users"));
		}

		try (TestDbOneTable second = new TestDbOneTable(file)) {
			Database dbAfter = getInternalDatabase(second);
			assertFalse(dbAfter.getAllTables().contains("orders"), "orders table should be dropped");
			assertTrue(dbAfter.getAllTables().contains("users"), "users table retained");
			// Existing data should remain (table not recreated due to identical schema)
			assertEquals(1, second.users.selectAll().size());
		}
	}

	@Test
	@DisplayName("Reinitialization with identical schema reuses existing tables (no drop/create)")
	void reusesExistingTables(@TempDir Path tempDir) throws Exception {
		String file = tempDir.resolve("nano-reuse.db").toString();
		List<String> tablesBefore;
		try (TestDb first = new TestDb(file)) {
			User u = new User(); u.id = 1; u.name = "Persist"; first.users.insert(u);
			// Capture table list after first init
			Database dbFirst = getInternalDatabase(first);
			tablesBefore = dbFirst.getAllTables();
		}
		// Second init with same definition
		try (TestDb second = new TestDb(file)) {
			Database dbSecond = getInternalDatabase(second);
			assertEquals(tablesBefore, dbSecond.getAllTables(), "Table list should be unchanged");
			assertEquals(1, second.users.selectAll().size(), "Row should persist across initialization");
		}
	}

	@Test
	@DisplayName("Throws when model has zero declared fields (unsupported)")
	void rejectsEmptyModel(@TempDir Path tempDir) {
		class EmptyModel extends BaseModel { /* no fields */ }
		class InvalidDb extends NanoDb {
			@SuppressWarnings("unused")
			public Table<EmptyModel> empties; // Should trigger UnsupportedOperationException from ClassToDataType
			InvalidDb(String filename) throws IOException, IllegalAccessException { super(filename); }
		}
		String dbFile = tempDir.resolve("nano-empty.db").toString();
		UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> new InvalidDb(dbFile));
		System.out.println("EmptyModel exception message: '" + ex.getMessage() + "'");
		assertNotNull(ex.getMessage()); // basic sanity
	}

	// Helper to access schema via reflection - InternalTable has getSchema through db.getTableSchema in NanoDb constructor
	private StructType getTableSchema(Table<?> table) throws Exception {
		Field internalField = Table.class.getDeclaredField("table");
		internalField.setAccessible(true);
		Object internalTable = internalField.get(table);
		// Invoke getSchema() method instead of accessing a non-existent field
		return (StructType) internalTable.getClass().getMethod("getSchema").invoke(internalTable);
	}

	private Database getInternalDatabase(NanoDb nano) throws Exception {
		Field dbField = NanoDb.class.getDeclaredField("db");
		dbField.setAccessible(true);
		return (Database) dbField.get(nano);
	}
}
