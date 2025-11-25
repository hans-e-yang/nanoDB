package org.nanodb.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.db.Database;
import org.nanodb.internal.tableFactory.LinkedListTableFactory;
import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Pair;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for database operations
 * Tests multi-table operations and schema management
 */
@DisplayName("Database Layer Integration Tests")
public class DatabaseIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private FileChunker chunker;
    private Database db;
    
    @BeforeEach
    void setUp() throws Exception {
        String dbFile = tempDir.resolve("db_integration.db").toString();
        chunker = new FileChunker(dbFile, 4096, 1000);
        LinkedListTableFactory factory = new LinkedListTableFactory(chunker);
        db = new Database(factory);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (chunker != null) {
            chunker.close();
        }
    }
    
    @Test
    @DisplayName("Multiple tables coexist and operate independently")
    void testMultipleTableOperations() throws Exception {
        // Create users table
        List<Pair<String, DataType<?>>> userFields = Arrays.asList(
            new Pair<>("id", new Int32Type()),
            new Pair<>("username", new VarCharType(30))
        );
        db.createTable("users", new StructType(userFields));
        
        // Create orders table
        List<Pair<String, DataType<?>>> orderFields = Arrays.asList(
            new Pair<>("order_id", new Int32Type()),
            new Pair<>("user_id", new Int32Type()),
            new Pair<>("amount", new Int32Type())
        );
        db.createTable("orders", new StructType(orderFields));
        
        // Insert into users
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1);
        user1.put("username", "alice");
        db.getTable("users").insert(List.of(user1));
        
        // Insert into orders
        Map<String, Object> order1 = new HashMap<>();
        order1.put("order_id", 100);
        order1.put("user_id", 1);
        order1.put("amount", 50);
        db.getTable("orders").insert(List.of(order1));
        
        // Verify independence
        assertEquals(1, db.getTable("users").selectAll().size());
        assertEquals(1, db.getTable("orders").selectAll().size());
    }
    
    @Test
    @DisplayName("Database persists metadata and tables across restarts")
    void testDatabasePersistence() throws Exception {
        // Create table and insert data
        List<Pair<String, DataType<?>>> fields = Arrays.asList(
            new Pair<>("id", new Int32Type()),
            new Pair<>("data", new VarCharType(20))
        );
        db.createTable("persistent", new StructType(fields));
        
        Map<String, Object> row = new HashMap<>();
        row.put("id", 42);
        row.put("data", "test_data");
        db.getTable("persistent").insert(List.of(row));
        
        // Close and reopen
        String dbFile = tempDir.resolve("db_integration.db").toString();
        chunker.close();
        
        chunker = new FileChunker(dbFile, 4096, 1000);
        LinkedListTableFactory factory = new LinkedListTableFactory(chunker);
        db = new Database(factory);
        
        // Verify table and data exist
        assertTrue(db.tableExists("persistent"));
        assertEquals(1, db.getTable("persistent").selectAll().size());
        assertEquals(42, db.getTable("persistent").selectAll().get(0).get("id"));
    }
    
    @Test
    @DisplayName("Complex schema with nested types works end-to-end")
    void testComplexSchemaOperations() throws Exception {
        // Create schema with array type
        List<Pair<String, DataType<?>>> fields = Arrays.asList(
            new Pair<>("id", new Int32Type()),
            new Pair<>("tags", new ArrayType<>(new VarCharType(10), 3))
        );
        db.createTable("complex", new StructType(fields));
        
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("tags", Arrays.asList("java", "db", "test"));
        
        db.getTable("complex").insert(List.of(row));
        
        List<Map<String, Object>> results = db.getTable("complex").selectAll();
        assertEquals(1, results.size());
        
        @SuppressWarnings("unchecked")
        List<String> retrievedTags = (List<String>) results.get(0).get("tags");
        assertEquals(3, retrievedTags.size());
        assertEquals("java", retrievedTags.get(0));
    }
}