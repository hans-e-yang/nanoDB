package org.nanodb.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.table.LinkedListTable;
import org.nanodb.internal.primitives.*;
import org.nanodb.internal.condition.*;
import org.nanodb.utils.Pair;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for storage layer components
 * Tests interaction between Chunker and LinkedListTable
 */
@DisplayName("Storage Layer Integration Tests")
public class StorageIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private FileChunker chunker;
    private StructType schema;
    
    @BeforeEach
    void setUp() throws Exception {
        String dbFile = tempDir.resolve("integration_test.db").toString();
        chunker = new FileChunker(dbFile, 4096, 1000);
        
        // Create a simple schema
        List<Pair<String, DataType<?>>> fields = Arrays.asList(
            new Pair<>("id", new Int32Type()),
            new Pair<>("name", new VarCharType(50)),
            new Pair<>("age", new Int32Type())
        );
        schema = new StructType(fields);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (chunker != null) {
            chunker.close();
        }
    }
    
    @Test
    @DisplayName("Table persists data across chunker operations")
    void testDataPersistenceAcrossChunks() throws Exception {
        LinkedListTable table = new LinkedListTable(chunker, schema);
        
        // Insert data that spans multiple chunks
        List<Map<String, Object>> largeDataset = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "User" + i);
            row.put("age", 20 + (i % 50));
            largeDataset.add(row);
        }
        
        table.insert(largeDataset);
        
        // Verify all data is retrievable
        List<Map<String, Object>> retrieved = table.selectAll();
        assertEquals(100, retrieved.size());
    }
    
    @Test
    @DisplayName("Table handles chunk boundary conditions correctly")
    void testChunkBoundaryConditions() throws Exception {
        LinkedListTable table = new LinkedListTable(chunker, schema);
        
        // Calculate exact capacity to test boundary
        int recordSize = schema.byteSize();
        int headerSize = 8; // 4 bytes next ptr + 4 bytes count
        int capacity = (chunker.getChunkSize() - headerSize) / recordSize;
        
        // Insert exactly capacity + 1 records to force new chunk creation
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < capacity + 1; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "Boundary" + i);
            row.put("age", 25);
            data.add(row);
        }
        
        table.insert(data);
        
        // Verify all records including the one in the new chunk
        assertEquals(capacity + 1, table.selectAll().size());
    }
    
    @Test
    @DisplayName("Concurrent operations maintain data integrity")
    void testConcurrentOperations() throws Exception {
        LinkedListTable table = new LinkedListTable(chunker, schema);
        
        // Insert initial data
        List<Map<String, Object>> initial = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "Initial" + i);
            row.put("age", 30);
            initial.add(row);
        }
        table.insert(initial);
        
        // Update some records
        int updated = table.update(
            new ComparisonCondition("age", ComparisonOperation.EQUAL, 30),
            row -> {
                row.put("age", 31);
                return row;
            },
            10
        );
        
        assertEquals(10, updated);
        
        // Delete some records
        int deleted = table.delete(
            new ComparisonCondition("age", ComparisonOperation.EQUAL, 31),
            5
        );
        
        assertEquals(5, deleted);
        
        // Verify final state
        assertEquals(15, table.selectAll().size());
    }
}