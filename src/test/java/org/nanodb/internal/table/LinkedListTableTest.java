package org.nanodb.internal.table;

import org.junit.jupiter.api.Test;
import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.chunker.SimpleMapChunker;
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.condition.ConditionVisitor;
import org.nanodb.internal.primitives.DataType;
import org.nanodb.internal.primitives.Int32Type;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.utils.Pair;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LinkedListTable that exercise the underlying LinkedListNode logic
 * (getNext(), getRecords(), updateNode(), createNewNode(), clear(), etc.)
 * indirectly via the public table API.
 */
public class LinkedListTableTest {

    private StructType createSingleIntSchema() {
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("v", new Int32Type()));
        return new StructType(fields);
    }

    @Test
    public void selectThrowsOnNullCondition() {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        assertThrows(MissingWhereConditionException.class,
            () -> table.select(null, 10));
    }

    @Test
    public void deleteThrowsOnNullCondition() {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        assertThrows(MissingWhereConditionException.class,
            () -> table.delete(null, 10));
    }

    @Test
    public void updateThrowsOnNullCondition() {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        assertThrows(MissingWhereConditionException.class,
            () -> table.update(null, row -> row, 10));
    }

    private Map<String, Object> row(int v) {
        Map<String, Object> m = new HashMap<>();
        m.put("v", v);
        return m;
    }

    /**
     * Condition that always returns true; used to ensure that delete/update
     * traverse all rows and therefore all nodes.
     */
    private static class TrueCondition implements Condition {
        @Override
        public boolean accept(ConditionVisitor visitor) {
            return true;
        }
    }

    @Test
    public void insertCreatesSecondNodeAndTraversalUsesGetNext() throws MissingWhereConditionException {
        // Use a very small chunk size so each node can hold only a few rows.
        SimpleMapChunker chunker = new SimpleMapChunker(24);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        // Insert many rows so that:
        // - The initial node fills up in the first while-loop
        // - The tail while-loop (with createNewNode) must run multiple times
        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            values.add(row(i));
        }

        table.insert(values);

        // Selecting with a large limit forces traversal across all nodes,
        // exercising getNext() and the inserted rows from tail nodes.
        List<Map<String, Object>> selected =
                table.select(new TrueCondition(), Integer.MAX_VALUE);

        assertEquals(100, selected.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, selected.get(i).get("v"));
        }
    }

     @Test
    public void clearDeletesAllNodes() throws MissingWhereConditionException {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        // Populate with some data (creating at least one node with rows).
        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            values.add(row(i));
        }
        table.insert(values);

        // Now clear the table; this should walk through nodes using getNext()
        // and invoke deleteNode() on each. It should not throw.
        table.clear();
    }

    @Test
    public void deleteAcrossMultipleNodesUsesUpdateNodeAndGetNext() throws MissingWhereConditionException {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            values.add(row(i));
        }
        table.insert(values);

        // Delete all rows where v < 10; this will cause each node
        // to rewrite its contents (updateNode) and traverse to next (getNext).
        Condition vLessThanTen = new ComparisonCondition("v", ComparisonOperation.LESS_THAN, 10);

        int deleted = table.delete(vLessThanTen, Integer.MAX_VALUE);
        assertEquals(10, deleted);

        List<Map<String, Object>> remaining =
                table.select(new TrueCondition(), Integer.MAX_VALUE);
        assertEquals(10, remaining.size());
        for (Map<String, Object> r : remaining) {
            int v = (Integer) r.get("v");
            assertTrue(v >= 10, "Only v >= 10 should remain");
        }
    }

    @Test
    public void updateAcrossMultipleNodesUsesUpdateNodeAndGetNext() throws MissingWhereConditionException {
        SimpleMapChunker chunker = new SimpleMapChunker(64);
        StructType schema = createSingleIntSchema();
        LinkedListTable table = new LinkedListTable(chunker, schema);

        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            values.add(row(i));
        }
        table.insert(values);

        Condition trueCond = new TrueCondition();

        int updated = table.update(trueCond, row -> {
            int v = (Integer) row.get("v");
            row.put("v", v + 1000);
            return row;
        }, Integer.MAX_VALUE);

        assertEquals(20, updated);

        List<Map<String, Object>> rows =
                table.select(new TrueCondition(), Integer.MAX_VALUE);
        assertEquals(20, rows.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(i + 1000, rows.get(i).get("v"));
        }
    }
}