package org.nanodb.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.cli.statement.StatementParser;
import org.nanodb.cli.executor.Executor;
import org.nanodb.cli.executor.ExecuteResult;
import org.nanodb.cli.statement.Statement;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.db.Database;
import org.nanodb.internal.tableFactory.LinkedListTableFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for frontend-backend interaction
 * Tests SQL parsing through to database execution
 */
@DisplayName("Frontend-Backend Integration Tests")
public class FrontendBackendIntegrationTest {

    @TempDir
    Path tempDir;

    private FileChunker chunker;
    private Database db;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() throws Exception {
        String dbFile = tempDir.resolve("frontend_test.db").toString();
        chunker = new FileChunker(dbFile, 4096, 1000);
        LinkedListTableFactory factory = new LinkedListTableFactory(chunker);
        db = new Database(factory);

        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        if (chunker != null) {
            chunker.close();
        }
    }

    @Test
    @DisplayName("Complete CRUD workflow through SQL interface")
    void testCompleteCRUDWorkflow() throws Exception {
        // CREATE
        Statement createStmt = StatementParser.parse(
                "CREATE TABLE employees (id int, name varchar(50), salary int)");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(createStmt, db));

        // INSERT
        Statement insertStmt1 = StatementParser.parse(
                "INSERT INTO employees (id, name, salary) VALUES (1, 'Alice', 75000)");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(insertStmt1, db));

        Statement insertStmt2 = StatementParser.parse(
                "INSERT INTO employees (id, name, salary) VALUES (2, 'Bob', 65000)");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(insertStmt2, db));

        // SELECT
        Statement selectStmt = StatementParser.parse(
                "SELECT * FROM employees WHERE salary >= 70000");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(selectStmt, db));

        String output = outputStream.toString();
        assertTrue(output.contains("Alice"));
        assertFalse(output.contains("Bob"));

        // UPDATE
        outputStream.reset();
        Statement updateStmt = StatementParser.parse(
                "UPDATE employees SET salary = 80000 WHERE name = 'Alice'");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(updateStmt, db));

        // Verify update
        outputStream.reset();
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(selectStmt, db));
        assertTrue(outputStream.toString().contains("80000"));

        // DELETE
        Statement deleteStmt = StatementParser.parse(
                "DELETE FROM employees WHERE id = 2");
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(deleteStmt, db));

        // Verify only 1 record remains
        assertEquals(1, db.getTable("employees").selectAll().size());
    }

    @Test
    @DisplayName("Complex queries with nested conditions work correctly")
    void testComplexQueryExecution() throws Exception {
        Statement createStmt = StatementParser.parse(
                "CREATE TABLE products (id int, name varchar(30), price int, category varchar(20))");
        Executor.execute(createStmt, db);

        // Insert test data
        String[] inserts = {
                "INSERT INTO products VALUES (1, 'Laptop', 1200, 'Electronics')",
                "INSERT INTO products VALUES (2, 'Mouse', 25, 'Electronics')",
                "INSERT INTO products VALUES (3, 'Desk', 300, 'Furniture')",
                "INSERT INTO products VALUES (4, 'Chair', 150, 'Furniture')"
        };

        for (String sql : inserts) {
            Executor.execute(StatementParser.parse(sql), db);
        }

        // Complex query: (category = 'Electronics' AND price > 30) OR (category =
        // 'Furniture' AND price < 200)
        Statement complexSelect = StatementParser.parse(
                "SELECT * FROM products WHERE (category = 'Electronics' AND price > 30) OR (category = 'Furniture' AND price < 200)");

        outputStream.reset();
        assertEquals(ExecuteResult.SUCCESS, Executor.execute(complexSelect, db));

        String output = outputStream.toString();
        assertTrue(output.contains("Laptop"));
        assertTrue(output.contains("Chair"));
        assertFalse(output.contains("Mouse"));
        assertFalse(output.contains("Desk"));
    }

    @Test
    @DisplayName("Transaction-like behavior maintains consistency")
    void testDataConsistency() throws Exception {
        Statement createStmt = StatementParser.parse(
                "CREATE TABLE accounts (id int, balance int)");
        Executor.execute(createStmt, db);

        Executor.execute(StatementParser.parse("INSERT INTO accounts VALUES (1, 1000)"), db);
        Executor.execute(StatementParser.parse("INSERT INTO accounts VALUES (2, 500)"), db);

        // Simulate transfer: deduct from account 1
        Executor.execute(StatementParser.parse("UPDATE accounts SET balance = 800 WHERE id = 1"), db);

        // Add to account 2
        Executor.execute(StatementParser.parse("UPDATE accounts SET balance = 700 WHERE id = 2"), db);

        // Verify total is preserved
        var accounts = db.getTable("accounts").selectAll();
        int total = accounts.stream()
                .mapToInt(row -> (Integer) row.get("balance"))
                .sum();

        assertEquals(1500, total, "Total balance should be preserved");
    }
}