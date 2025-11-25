package org.nanodb.orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nanodb.exception.MissingWhereConditionException;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.condition.ConditionVisitor;
import org.nanodb.internal.primitives.Int32Type;
import org.nanodb.internal.primitives.VarCharType;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.utils.Pair;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ORM {@link Table} wrapper validating serialization
 * and delegation to underlying InternalTable semantics.
 */
public class TableTest {

	// Simple model
	public static class User extends BaseModel { int id; String name; }

	// Condition that always evaluates true
	static class TrueCondition implements Condition { @Override public boolean accept(ConditionVisitor visitor) { return true; } }

	// Fake InternalTable implementation storing rows in-memory
	static class FakeInternalTable extends org.nanodb.internal.table.InternalTable {
		private final List<Map<String,Object>> rows = new ArrayList<>();
		private final StructType schema;
		FakeInternalTable() {
			schema = new StructType(List.of(
					new Pair<>("id", new Int32Type()),
					new Pair<>("name", new VarCharType(50))
			));
		}
		@Override public StructType getSchema() { return schema; }
		@Override public int getInitialId() { return 0; }

		@Override public List<Map<String, Object>> select(Condition condition, int limit) throws MissingWhereConditionException {
			List<Map<String,Object>> out = new ArrayList<>();
			for (int i=0; i<rows.size() && out.size()<limit; i++) out.add(rows.get(i));
			return out;
		}
		@Override public int delete(Condition condition, int limit) throws MissingWhereConditionException {
			int toDelete = Math.min(limit, rows.size());
			for (int i=0; i<toDelete; i++) rows.remove(0);
			return toDelete;
		}
		@Override public int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit) throws MissingWhereConditionException {
			int count = 0;
			for (int i=0; i<rows.size() && count<limit; i++) {
				rows.set(i, updateFunction.apply(rows.get(i)));
				count++;
			}
			return count;
		}
		@Override public void insert(List<Map<String, Object>> values) { rows.addAll(values); }
		@Override public void clear() { rows.clear(); }
		public List<Map<String,Object>> getRows() { return rows; }
	}

	private Table<User> createTable(FakeInternalTable internal) {
		return new Table<>(internal, User.class);
	}

	@Test
	@DisplayName("Insert serializes model fields into underlying maps")
	void testInsertSerializes() {
		FakeInternalTable internal = new FakeInternalTable();
		Table<User> table = createTable(internal);
		User u = new User(); u.id = 42; u.name = "Alice";
		table.insert(List.of(u));
		assertEquals(1, internal.getRows().size());
		Map<String,Object> stored = internal.getRows().get(0);
		assertEquals(42, stored.get("id"));
		assertEquals("Alice", stored.get("name"));
	}

	@Test
	@DisplayName("Select deserializes maps back into model instances")
	void testSelectDeserializes() throws Exception {
		FakeInternalTable internal = new FakeInternalTable();
		internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B")
		));
		Table<User> table = createTable(internal);
		List<User> results = table.select(new TrueCondition(), 10);
		assertEquals(2, results.size());
		assertEquals(1, results.get(0).id);
		assertEquals("A", results.get(0).name);
		assertEquals(2, results.get(1).id);
		assertEquals("B", results.get(1).name);
	}

	@Test
	@DisplayName("Update applies transformation through serialization pipeline")
	void testUpdateTransforms() throws Exception {
		FakeInternalTable internal = new FakeInternalTable();
		internal.insert(List.of(Map.of("id", 5, "name", "Old")));
		Table<User> table = createTable(internal);
		int updated = table.update(new TrueCondition(), user -> { user.name = "New"; return user; }, 10);
		assertEquals(1, updated);
		assertEquals("New", internal.getRows().get(0).get("name"));
	}

	@Test
	@DisplayName("Delete removes rows from underlying table honoring limit")
	void testDeleteRemoves() throws Exception {
		FakeInternalTable internal = new FakeInternalTable();
		internal.insert(List.of(
				Map.of("id", 1, "name", "A"),
				Map.of("id", 2, "name", "B")
		));
		Table<User> table = createTable(internal);
		int deleted = table.delete(new TrueCondition(), 1);
		assertEquals(1, deleted);
		assertEquals(1, internal.getRows().size());
	}

	@Test
	@DisplayName("Clear wipes all underlying rows")
	void testClear() {
		FakeInternalTable internal = new FakeInternalTable();
		Table<User> table = createTable(internal);
		User u1 = new User(); u1.id = 1; u1.name = "A";
		User u2 = new User(); u2.id = 2; u2.name = "B";
		table.insert(List.of(u1, u2));
		assertFalse(internal.getRows().isEmpty());
		table.clear();
		assertTrue(internal.getRows().isEmpty());
	}
}
