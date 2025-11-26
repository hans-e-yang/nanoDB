package org.nanodb.orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.condition.ConditionVisitor;
import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Pair;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ORM {@link Table} wrapper validating serialization,
 * null-to-default conversion, and delegation to underlying InternalTable.
 */
public class TableTest {

	// ==================== Test Models ====================

	public static class User extends BaseModel {
		public int id;
		public String name;
		public User() {}
	}

	public static class FullModel extends BaseModel {
		public int intField;
		public String stringField;
		public Boolean boolField;  // Wrapper to allow null
		public Date dateField;
		public byte[] bytesField;
		public List<Integer> listField;
		public FullModel() {}
	}

	public static class Address extends BaseModel {
		public String street;
		public String city;
		public Address() {}
	}

	public static class Person extends BaseModel {
		public int id;
		public String name;
		public Address address;
		public Person() {}
	}

	public static class UnknownModel extends BaseModel {
		public int id;
		public String unknownField;
		public UnknownModel() {}
	}

	// ==================== Test Infrastructure ====================

	static class TrueCondition implements Condition {
		@Override public boolean accept(ConditionVisitor visitor) { return true; }
	}

	/** Generic fake InternalTable that works with any schema */
	static class FakeInternalTable extends org.nanodb.internal.table.InternalTable {
		private final List<Map<String,Object>> rows = new ArrayList<>();
		private final StructType schema;

		FakeInternalTable(StructType schema) {
			this.schema = schema;
		}

		@Override public StructType getSchema() { return schema; }
		@Override public int getInitialId() { return 0; }

		@Override public List<Map<String, Object>> select(Condition condition, int limit) throws MissingWhereConditionException {
			List<Map<String,Object>> out = new ArrayList<>();
			for (int i = 0; i < rows.size() && out.size() < limit; i++) {
				out.add(rows.get(i));
			}
			return out;
		}

		@Override public int delete(Condition condition, int limit) throws MissingWhereConditionException {
			int toDelete = Math.min(limit, rows.size());
			for (int i = 0; i < toDelete; i++) rows.remove(0);
			return toDelete;
		}

		@Override public int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit) throws MissingWhereConditionException {
			int count = 0;
			for (int i = 0; i < rows.size() && count < limit; i++) {
				rows.set(i, updateFunction.apply(rows.get(i)));
				count++;
			}
			return count;
		}

		@Override public void insert(List<Map<String, Object>> values) { rows.addAll(values); }
		@Override public void clear() { rows.clear(); }
		List<Map<String,Object>> getRows() { return rows; }
	}

	// Schema builders
	private static StructType userSchema() {
		return new StructType(List.of(
			new Pair<>("id", new Int32Type()),
			new Pair<>("name", new VarCharType(50))
		));
	}

	private static StructType fullModelSchema() {
		return new StructType(List.of(
			new Pair<>("boolField", new BooleanType()),
			new Pair<>("bytesField", new BytesType(100)),
			new Pair<>("dateField", new DateType()),
			new Pair<>("intField", new Int32Type()),
			new Pair<>("listField", new ArrayType<>(new Int32Type(), 10)),
			new Pair<>("stringField", new VarCharType(100))
		));
	}

	private static StructType personSchema() {
		StructType addressSchema = new StructType(List.of(
			new Pair<>("city", new VarCharType(50)),
			new Pair<>("street", new VarCharType(100))
		));
		return new StructType(List.of(
			new Pair<>("address", addressSchema),
			new Pair<>("id", new Int32Type()),
			new Pair<>("name", new VarCharType(50))
		));
	}

	private static StructType unknownTypeSchema() {
		List<Pair<String, DataType<?>>> fields = new ArrayList<>();
		fields.add(new Pair<>("id", new Int32Type()));
		fields.add(new Pair<>("unknownField", new DataType<String>() {
			@Override public byte[] toBytes(String value) { return value.getBytes(); }
			@Override public String fromBytes(byte[] bytes, int offset) { return new String(bytes); }
			@Override public int byteSize() { return 100; }
			@Override public <T2> T2 accept(DataTypeVisitor<T2> visitor) { return null; }
		}));
		return new StructType(fields);
	}

	// ==================== Basic CRUD Operations ====================

	@Nested
	@DisplayName("Basic CRUD Operations")
	class BasicCrudTests {

		@Test
		@DisplayName("Insert serializes model fields into underlying maps")
		void testInsert() {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);

			User u = new User();
			u.id = 42;
			u.name = "Alice";
			table.insert(List.of(u));

			assertEquals(1, internal.getRows().size());
			assertEquals(42, internal.getRows().get(0).get("id"));
			assertEquals("Alice", internal.getRows().get(0).get("name"));
		}

		@Test
		@DisplayName("Insert multiple items in bulk")
		void testInsertMultiple() {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);

			User u1 = new User(); u1.id = 1; u1.name = "Alice";
			User u2 = new User(); u2.id = 2; u2.name = "Bob";
			table.insert(List.of(u1, u2));

			assertEquals(2, internal.getRows().size());
		}

		@Test
		@DisplayName("Insert empty list does nothing")
		void testInsertEmpty() {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);
			table.insert(List.of());
			assertTrue(internal.getRows().isEmpty());
		}

		@Test
		@DisplayName("Select deserializes maps back into model instances")
		void testSelect() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B")
			));

			Table<User> table = new Table<>(internal, User.class);
			List<User> results = table.select(new TrueCondition(), 10);

			assertEquals(2, results.size());
			assertEquals(1, results.get(0).id);
			assertEquals("A", results.get(0).name);
		}

		@Test
		@DisplayName("Select on empty table returns empty list")
		void testSelectEmpty() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);
			assertTrue(table.select(new TrueCondition(), 10).isEmpty());
		}

		@Test
		@DisplayName("Select respects limit parameter")
		void testSelectWithLimit() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B"),
				Map.of("id", 3, "name", "C")
			));

			Table<User> table = new Table<>(internal, User.class);
			assertEquals(2, table.select(new TrueCondition(), 2).size());
		}

		@Test
		@DisplayName("Update applies transformation")
		void testUpdate() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			internal.insert(List.of(Map.of("id", 5, "name", "Old")));

			Table<User> table = new Table<>(internal, User.class);
			int updated = table.update(new TrueCondition(), user -> {
				user.name = "New";
				return user;
			}, 10);

			assertEquals(1, updated);
			assertEquals("New", internal.getRows().get(0).get("name"));
		}

		@Test
		@DisplayName("Update on empty table returns zero")
		void testUpdateEmpty() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);
			assertEquals(0, table.update(new TrueCondition(), u -> u, 10));
		}

		@Test
		@DisplayName("Update respects limit parameter")
		void testUpdateWithLimit() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B"),
				Map.of("id", 3, "name", "C")
			));

			Table<User> table = new Table<>(internal, User.class);
			int updated = table.update(new TrueCondition(), u -> { u.name = "X"; return u; }, 2);

			assertEquals(2, updated);
			assertEquals("X", internal.getRows().get(0).get("name"));
			assertEquals("C", internal.getRows().get(2).get("name")); // Not updated
		}

		@Test
		@DisplayName("Delete removes rows")
		void testDelete() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B")
			));

			Table<User> table = new Table<>(internal, User.class);
			assertEquals(1, table.delete(new TrueCondition(), 1));
			assertEquals(1, internal.getRows().size());
		}

		@Test
		@DisplayName("Delete from empty table returns zero")
		void testDeleteEmpty() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);
			assertEquals(0, table.delete(new TrueCondition(), 10));
		}

		@Test
		@DisplayName("Clear wipes all rows")
		void testClear() {
			FakeInternalTable internal = new FakeInternalTable(userSchema());
			Table<User> table = new Table<>(internal, User.class);

			User u = new User(); u.id = 1; u.name = "A";
			table.insert(List.of(u));
			assertFalse(internal.getRows().isEmpty());

			table.clear();
			assertTrue(internal.getRows().isEmpty());
		}
	}

	// ==================== All Data Types ====================

	@Nested
	@DisplayName("All Data Types Support")
	class AllDataTypesTests {

		@Test
		@DisplayName("Insert with all supported types serializes correctly")
		void testInsertAllTypes() {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Table<FullModel> table = new Table<>(internal, FullModel.class);

			FullModel m = new FullModel();
			m.intField = 42;
			m.stringField = "test";
			m.boolField = true;
			m.dateField = new Date(1000000000000L);
			m.bytesField = new byte[]{1, 2, 3};
			m.listField = List.of(10, 20, 30);
			table.insert(List.of(m));

			Map<String,Object> stored = internal.getRows().get(0);
			assertEquals(42, stored.get("intField"));
			assertEquals("test", stored.get("stringField"));
			assertEquals(true, stored.get("boolField"));
			assertEquals(new Date(1000000000000L), stored.get("dateField"));
			assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) stored.get("bytesField"));
			assertEquals(List.of(10, 20, 30), stored.get("listField"));
		}

		@Test
		@DisplayName("Select with all types deserializes correctly")
		void testSelectAllTypes() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Map<String, Object> row = new HashMap<>();
			row.put("intField", 100);
			row.put("stringField", "hello");
			row.put("boolField", true);
			row.put("dateField", new Date(2000000000000L));
			row.put("bytesField", new byte[]{9, 8, 7});
			row.put("listField", List.of(1, 2, 3));
			internal.insert(List.of(row));

			Table<FullModel> table = new Table<>(internal, FullModel.class);
			FullModel result = table.select(new TrueCondition(), 10).get(0);

			assertEquals(100, result.intField);
			assertEquals("hello", result.stringField);
			assertTrue(result.boolField);
			assertEquals(new Date(2000000000000L), result.dateField);
			assertArrayEquals(new byte[]{9, 8, 7}, result.bytesField);
			assertEquals(List.of(1, 2, 3), result.listField);
		}

		@Test
		@DisplayName("Update with all types transforms correctly")
		void testUpdateAllTypes() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Map<String, Object> row = new HashMap<>();
			row.put("intField", 1);
			row.put("stringField", "old");
			row.put("boolField", false);
			row.put("dateField", new Date(0));
			row.put("bytesField", new byte[0]);
			row.put("listField", List.of());
			internal.insert(List.of(row));

			Table<FullModel> table = new Table<>(internal, FullModel.class);
			table.update(new TrueCondition(), m -> {
				m.intField = 999;
				m.stringField = "new";
				m.boolField = true;
				m.dateField = new Date(1234567890000L);
				m.bytesField = new byte[]{42};
				m.listField = List.of(100);
				return m;
			}, 10);

			Map<String,Object> stored = internal.getRows().get(0);
			assertEquals(999, stored.get("intField"));
			assertEquals("new", stored.get("stringField"));
			assertEquals(true, stored.get("boolField"));
			assertEquals(new Date(1234567890000L), stored.get("dateField"));
			assertArrayEquals(new byte[]{42}, (byte[]) stored.get("bytesField"));
			assertEquals(List.of(100), stored.get("listField"));
		}
	}

	// ==================== Null to Default Conversion ====================

	@Nested
	@DisplayName("Null to Default Conversion")
	class NullToDefaultTests {

		@Test
		@DisplayName("Insert converts all null fields to defaults")
		void testInsertNullsToDefaults() {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Table<FullModel> table = new Table<>(internal, FullModel.class);

			FullModel m = new FullModel();
			// All object fields are null, primitives have default values
			table.insert(List.of(m));

			Map<String,Object> stored = internal.getRows().get(0);
			assertEquals(0, stored.get("intField"));
			assertEquals("", stored.get("stringField"));
			assertEquals(false, stored.get("boolField"));  // null Boolean -> false
			assertEquals(new Date(0), stored.get("dateField"));
			assertArrayEquals(new byte[0], (byte[]) stored.get("bytesField"));
			assertEquals(List.of(), stored.get("listField"));
		}

		@Test
		@DisplayName("Insert with mix of null and non-null fields")
		void testInsertMixedNullAndNonNull() {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Table<FullModel> table = new Table<>(internal, FullModel.class);

			FullModel m = new FullModel();
			m.intField = 42;
			m.stringField = "hello";
			m.boolField = true;
			// dateField, bytesField, listField are null

			table.insert(List.of(m));

			Map<String,Object> stored = internal.getRows().get(0);
			assertEquals(42, stored.get("intField"));
			assertEquals("hello", stored.get("stringField"));
			assertEquals(true, stored.get("boolField"));
			assertEquals(new Date(0), stored.get("dateField"));
			assertArrayEquals(new byte[0], (byte[]) stored.get("bytesField"));
			assertEquals(List.of(), stored.get("listField"));
		}

		@Test
		@DisplayName("Update setting fields to null converts to defaults")
		void testUpdateNullsToDefaults() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(fullModelSchema());
			Map<String, Object> row = new HashMap<>();
			row.put("intField", 100);
			row.put("stringField", "original");
			row.put("boolField", true);
			row.put("dateField", new Date(1000000000000L));
			row.put("bytesField", new byte[]{1, 2, 3});
			row.put("listField", List.of(1, 2, 3));
			internal.insert(List.of(row));

			Table<FullModel> table = new Table<>(internal, FullModel.class);
			table.update(new TrueCondition(), m -> {
				m.stringField = null;
				m.boolField = null;
				m.dateField = null;
				m.bytesField = null;
				m.listField = null;
				return m;
			}, 10);

			Map<String,Object> stored = internal.getRows().get(0);
			assertEquals("", stored.get("stringField"));
			assertEquals(false, stored.get("boolField"));
			assertEquals(new Date(0), stored.get("dateField"));
			assertArrayEquals(new byte[0], (byte[]) stored.get("bytesField"));
			assertEquals(List.of(), stored.get("listField"));
		}

		@Test
		@DisplayName("Unknown data type falls back to empty string default")
		void testUnknownTypeFallback() {
			FakeInternalTable internal = new FakeInternalTable(unknownTypeSchema());
			Table<UnknownModel> table = new Table<>(internal, UnknownModel.class);

			UnknownModel m = new UnknownModel();
			m.id = 1;
			m.unknownField = null;
			table.insert(List.of(m));

			assertEquals("", internal.getRows().get(0).get("unknownField"));
		}
	}

	// ==================== Nested Struct Type ====================

	@Nested
	@DisplayName("Nested StructType Support")
	class NestedStructTests {

		@Test
		@DisplayName("Insert with nested model serializes correctly")
		void testInsertNested() {
			FakeInternalTable internal = new FakeInternalTable(personSchema());
			Table<Person> table = new Table<>(internal, Person.class);

			Person p = new Person();
			p.id = 1;
			p.name = "John";
			p.address = new Address();
			p.address.street = "123 Main St";
			p.address.city = "New York";
			table.insert(List.of(p));

			@SuppressWarnings("unchecked")
			Map<String, Object> addressMap = (Map<String, Object>) internal.getRows().get(0).get("address");
			assertEquals("123 Main St", addressMap.get("street"));
			assertEquals("New York", addressMap.get("city"));
		}

		@Test
		@DisplayName("Select with nested model deserializes correctly")
		void testSelectNested() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(personSchema());
			Map<String, Object> addressMap = new HashMap<>();
			addressMap.put("street", "456 Oak Ave");
			addressMap.put("city", "Boston");
			Map<String, Object> row = new HashMap<>();
			row.put("id", 2);
			row.put("name", "Jane");
			row.put("address", addressMap);
			internal.insert(List.of(row));

			Table<Person> table = new Table<>(internal, Person.class);
			Person result = table.select(new TrueCondition(), 10).get(0);

			assertNotNull(result.address);
			assertEquals("456 Oak Ave", result.address.street);
			assertEquals("Boston", result.address.city);
		}

		@Test
		@DisplayName("Null nested model converts to default struct")
		void testNullNestedToDefault() {
			FakeInternalTable internal = new FakeInternalTable(personSchema());
			Table<Person> table = new Table<>(internal, Person.class);

			Person p = new Person();
			p.id = 1;
			p.name = "Homeless";
			p.address = null;
			table.insert(List.of(p));

			@SuppressWarnings("unchecked")
			Map<String, Object> addressMap = (Map<String, Object>) internal.getRows().get(0).get("address");
			assertNotNull(addressMap);
			assertEquals("", addressMap.get("street"));
			assertEquals("", addressMap.get("city"));
		}

		@Test
		@DisplayName("Update setting nested struct to null converts to defaults")
		void testUpdateNestedToNull() throws Exception {
			FakeInternalTable internal = new FakeInternalTable(personSchema());
			Map<String, Object> addressMap = new HashMap<>();
			addressMap.put("street", "123 Main");
			addressMap.put("city", "NYC");
			Map<String, Object> row = new HashMap<>();
			row.put("id", 1);
			row.put("name", "John");
			row.put("address", addressMap);
			internal.insert(List.of(row));

			Table<Person> table = new Table<>(internal, Person.class);
			table.update(new TrueCondition(), p -> {
				p.address = null;
				return p;
			}, 10);

			@SuppressWarnings("unchecked")
			Map<String, Object> storedAddress = (Map<String, Object>) internal.getRows().get(0).get("address");
			assertEquals("", storedAddress.get("street"));
			assertEquals("", storedAddress.get("city"));
		}
	}
}
