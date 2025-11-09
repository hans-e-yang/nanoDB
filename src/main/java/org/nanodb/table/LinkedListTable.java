package org.nanodb.table;

import org.nanodb.chunker.Chunker;
import org.nanodb.condition.ComparisonCondition;
import org.nanodb.condition.Condition;
import org.nanodb.condition.ConditionVisitor;
import org.nanodb.conditionVisitors.ValueVisitor;
import org.nanodb.primitives.Int32Type;
import org.nanodb.primitives.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple linked list implementation that uses a byte array to manage
 * the list structure. The first 4 bytes are used as a pointer to the
 * next node, and the next 4 bytes store the count of the records.
 *
 * <p>Each chunk in the linked list consists of:</p>
 * <ul>
 *   <li>4 bytes for the next pointer</li>
 *   <li>4 bytes for the count of records</li>
 *   <li>Rest of the bytes are for the unspanned records/rows</li>
 * </ul>
 *
 */
public class LinkedListTable implements Table {
    private Chunker chunker;
    private int startingBlockId;
    private int recordSize;
    private StructType recordType;

    public LinkedListTable(Chunker chunker, int startingBlockId, int recordSize, StructType recordType) {
        this.chunker = chunker;
        this.startingBlockId = startingBlockId;
        this.recordSize = recordSize;
        this.recordType = recordType;
    }

    public List<Map<String, Object>> select(ComparisonCondition condition) {
        return select(condition, Integer.MAX_VALUE);
    }
    public List<Map<String, Object>> select(ComparisonCondition condition, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        byte[] chunk = chunker.getChunk(startingBlockId);
        Int32Type int32 = new Int32Type();

        // Loop through all chunks
        while (chunk.length != 0) {
            int offset = 0;
            Integer nextChunk = int32.fromBytes(chunk, 0);
            Integer records = int32.fromBytes(chunk, int32.byteSize());
            offset += int32.byteSize() * 2;

            for (int i = 0; i < records; i++) {
                Map<String, Object> row = recordType.fromBytes(chunk, offset);
                offset += int32.byteSize() * 2;

                // compare the row with the condition
                ConditionVisitor comparer = new ValueVisitor(row);
                if (condition.accept(comparer)) {
                    results.add(row);
                }

                // End the function early if hit limit
                if (results.size() >= limit) return results;
            }

            chunk = chunker.getChunk(nextChunk);
        }

        return results;
    }

    // Returns the count of deleted rows
    public int delete(Condition condition) { return delete(condition, Integer.MAX_VALUE);}
    public int delete(Condition condition, int limit) {
        int deleted = 0;

        byte[] chunk = chunker.getChunk(startingBlockId);
        Int32Type int32 = new Int32Type();

        // Loop through all chunks
        while (chunk.length != 0) {
            List<Map<String, Object>> kept = new ArrayList<>();
            int offset = 0;
            Integer nextChunk = int32.fromBytes(chunk, 0);
            Integer records = int32.fromBytes(chunk, int32.byteSize());
            offset += int32.byteSize() * 2;

            // Loop through all the rows
            for (int i = 0; i < records; i++) {
                Map<String, Object> row = recordType.fromBytes(chunk, offset);
                offset += int32.byteSize() * 2;

                // compare the row with the condition
                ConditionVisitor comparer = new ValueVisitor(row);
                if (condition.accept(comparer)) {
                    kept.add(row);
                } else {
                    deleted++;
                }

                if (deleted >= limit) break;
            }

            // Rewrite the current chunk
            byte[] data = int32

            chunk = chunker.getChunk(nextChunk);
        }
    }

    public byte[] update(Function<Index, Boolean> indexFn, Function<byte[], Boolean> valFn, Function<byte[], byte[]> updateFn) {

    }

    public void insert(byte[] value) {

    }

    public void clear() {

    }
}
