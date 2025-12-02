package org.nanodb.system;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import org.nanodb.Main;

import static org.junit.jupiter.api.Assertions.*;

class CLISystemTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private InputStream originalIn;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        originalOut = System.out;
        originalIn = System.in;
        System.setOut(new PrintStream(outputStream));
        tempDir = Files.createTempDirectory("nanodb-e2e-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setIn(originalIn);
        outputStream.reset();
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    @DisplayName("End-to-end CLI workflow: create, insert, select, update, delete, error handling")
    void testCliEndToEndWorkflow() throws Exception {
        String input = String.join(System.lineSeparator(),
                "CREATE TABLE students (id int, name varchar(50))",
                "INSERT INTO students (id, name) VALUES (1, 'Alice')",
                "INSERT INTO students (id, name) VALUES (2, 'Bob')",
                "SELECT * FROM students",
                "UPDATE students SET name = 'Charlie' WHERE id = 2",
                "SELECT * FROM students WHERE id = 2",
                "DELETE FROM students WHERE id = 1",
                "SELECT * FROM students",
                "DROP TABLE students",
                "INSERT INTO students (id, name) VALUES (3, 'Eve')", // should error
                "exit");
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        String dbFile = tempDir.resolve("e2e_test.db").toString();
        Main.main(new String[] { dbFile });
        String output = outputStream.toString();

        // Check for successful table creation
        assertTrue(output.contains("Table 'students' created"), "Table creation should succeed");
        // Check for successful inserts
        assertTrue(output.contains("Row inserted"), "Rows should be inserted");
        // Check select output contains both Alice and Bob
        assertTrue(output.contains("Alice"), "Select should return Alice");
        assertTrue(output.contains("Bob"), "Select should return Bob");
        // Check update reflected in select
        assertTrue(output.contains("Charlie"), "Update should change Bob to Charlie");
        // Check delete was successful
        assertTrue(output.contains("Deleted 1 row(s)"), "Delete should succeed");
        // Check drop table
        assertTrue(output.contains("Table 'students' dropped"), "Table should be dropped");
        // Check error for insert after drop
        assertTrue(output.contains("TABLE_NOT_FOUND"), "Insert after drop should error");
        // Check for exit message
        assertTrue(output.contains("Goodbye!"), "Should print goodbye message");
    }
}
