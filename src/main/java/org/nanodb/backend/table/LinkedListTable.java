package org.nanodb.backend.table;

import org.nanodb.backend.chunker.Chunker;
import org.nanodb.backend.condition.Condition;
import org.nanodb.backend.condition.ConditionVisitor;
import org.nanodb.backend.condition.visitor.ValueVisitor;
import org.nanodb.backend.primitives.Int32Type;
import org.nanodb.backend.primitives.StructType;
import org.nanodb.backend.primitives.Reader;
import org.nanodb.utils.ByteExtensions;

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
public class LinkedListTable extends Table {
    private final Chunker chunker;
    private final int startingBlockId;
    private final StructType recordType;

    private static class LinkedListNode {
        private static final Int32Type int32 = new Int32Type();
        private final int currentId;
        private int nextPtr;
        private final List<Map<String, Object>> records;

        private final Chunker chunker;
        private final StructType recordType;

        public LinkedListNode(Chunker chunker, StructType recordType, int blockId) {
            this.currentId = blockId;
            this.chunker = chunker;
            this.recordType = recordType;

            byte[] chunk = chunker.getChunk(blockId);
            Reader reader = new Reader(chunk);
            nextPtr = reader.read(int32);
            int recordCount = reader.read(int32);
            records = new ArrayList<>();

            for (int i = 0; i < recordCount; i++) {
                records.add(reader.read(recordType));
            }
        }

        public LinkedListNode getNext() {
            if (nextPtr <= 0) {return null;}
            return new LinkedListNode(chunker, recordType, nextPtr);
        }

        public List<Map<String, Object>> getRecords() {
            return records;
        }

        public void updateNode(List<Map<String, Object>> newRecords) {
            if (newRecords.size() * recordType.byteSize() + 2*int32.byteSize() > chunker.getChunkSize()) {
                throw new IllegalArgumentException("Too many data to write in a single chunk!");
            }

            // Rewrite the current chunk
            
            List<byte[]> data = new ArrayList<>();
            data.add(int32.toBytes(this.nextPtr));
            data.add(int32.toBytes(newRecords.size()));
            for (Map<String, Object> row : newRecords) data.add(recordType.toBytes(row));

            chunker.updateChunk(
                this.currentId, 
                0, 
                ByteExtensions.concat(data)
            );
        }

        public LinkedListNode createNewNode() {
            nextPtr = chunker.createNewChunk();
            // Update the ptr in the underlying data
            chunker.updateChunk(this.currentId, 0, int32.toBytes(nextPtr));
            return getNext();
        }

        public void deleteNode() {
            chunker.deleteChunk(this.currentId);
        }

        public int getCapacity() {
            int usableSpace = chunker.getChunkSize() - (2 * int32.byteSize());
            return usableSpace / recordType.byteSize();
        }
    }

    public LinkedListTable(Chunker chunker, StructType recordType) {
        int newChunkId = chunker.createNewChunk();
        this.chunker = chunker;
        this.startingBlockId = newChunkId;
        this.recordType = recordType;
    }

    @Override
    public int getInitialId() {
        return startingBlockId;
    }

    @Override
    public StructType getSchema() {
        return recordType;
    }

    /**
     * Gets the table located at the starting block Id
     * @param chunker
     * @param startingBlockId
     * @param recordType
     */
    public LinkedListTable(Chunker chunker, int startingBlockId, StructType recordType) {
        this.chunker = chunker;
        this.startingBlockId = startingBlockId;
        this.recordType = recordType;
    }

    public List<Map<String, Object>> select(Condition condition) {
        return select(condition, Integer.MAX_VALUE);
    }
    public List<Map<String, Object>> select(Condition condition, int limit) {
        List<Map<String, Object>> results = new ArrayList<>();

        LinkedListNode node = new LinkedListNode(this.chunker, this.recordType, this.startingBlockId);

        // Loop through all chunks
        while (node != null) {
            for (Map<String, Object> row : node.getRecords()) {
                // compare the row with the condition
                ConditionVisitor comparer = new ValueVisitor(row);
                if (condition.accept(comparer)) {
                    results.add(row);
                }

                // End the function early if hit limit
                if (results.size() >= limit) return results;
            }
            node = node.getNext();
        }

        return results;
    }

    // Returns the count of deleted rows
    public int delete(Condition condition) { return delete(condition, Integer.MAX_VALUE);}
    public int delete(Condition condition, int limit) {
        int deleted = 0;

        // Clean all nodes, but we do not deallocate them
        // May change implementation later
        LinkedListNode node = new LinkedListNode(this.chunker, this.recordType, this.startingBlockId);

        // Loop through all chunks
        while (node != null) {
            List<Map<String, Object>> kept = new ArrayList<>();

            // Loop through all the rows
            for (Map<String, Object> row : node.getRecords()) {
                // compare the row with the condition
                ConditionVisitor comparer = new ValueVisitor(row);
                if (condition.accept(comparer)) {
                    kept.add(row);
                } else {
                    deleted++;
                }

                if (deleted >= limit) break;
            }

            node.updateNode(kept);
            node = node.getNext();
        }

        return deleted;
    }


    public int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction) {
        return update(condition, updateFunction, Integer.MAX_VALUE);
    }
    public int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit) {
        int updated = 0;

        // Clean all nodes, but we do not deallocate them
        // May change implementation later
        LinkedListNode node = new LinkedListNode(this.chunker, this.recordType, this.startingBlockId);

        // Loop through all chunks
        while (node != null) {
            List<Map<String, Object>> newRows = new ArrayList<>();

            // Loop through all the rows
            for (Map<String, Object> row : node.getRecords()) {
                // compare the row with the condition
                ConditionVisitor comparer = new ValueVisitor(row);
                if (condition.accept(comparer)) {
                    newRows.add(updateFunction.apply(row));
                } else {
                    newRows.add(row);
                }

                if (updated >= limit) break;
            }

            node.updateNode(newRows);
            node = node.getNext();
        }

        return updated;
    }

    public void insert(List<Map<String, Object>> values) {
        // Initialization
        LinkedListNode node = new LinkedListNode(this.chunker, this.recordType, this.startingBlockId);
        LinkedListNode lastNode = node;
        int inserted = 0;

        // Loop through all nodes while there are uninserted values
        while (node != null && inserted < values.size()) {
            // Get the records in the current node
            List<Map<String, Object>> rows = node.getRecords();

            // Fill them with values to be inserted
            while (rows.size() < node.getCapacity() && inserted < values.size()) {
                rows.add(values.get(inserted++));
            }

            // Update the node
            node.updateNode(rows);

            lastNode = node;
            node = node.getNext();
        }

        // If we reached the end of the nodes, but not all values are inserted, we must create a new node
        node = lastNode;
        while (inserted < values.size()) {
            node = node.createNewNode();

            // Get the records in the current node
            List<Map<String, Object>> rows = node.getRecords();

            // Fill them with values to be inserted
            while (rows.size() < node.getCapacity() && inserted < values.size()) {
                rows.add(values.get(inserted++));
            }

            // Update the node
            node.updateNode(rows);
        }
    }

    public void clear() {
        // Unlike delete, clear totally destroys all allocated chunks
        LinkedListNode node = new LinkedListNode(this.chunker, this.recordType, this.startingBlockId);
        while (node != null) {
            node.deleteNode();
            node = node.getNext();
        }
    }
}
