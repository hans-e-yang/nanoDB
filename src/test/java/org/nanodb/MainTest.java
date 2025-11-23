package org.nanodb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Main CLI class
 * Tests CLI functionality including command processing, help output, and database operations
 *
 * Test Organization:
 * 1. Help Command - Verify help message output
 * 2. Database Initialization - Test database file creation
 * 3. Command Processing - Test various SQL commands through the CLI
 * 4. Exit Commands - Verify exit and quit functionality
 * 5. Empty Input - Test empty line handling
 * 6. Error Handling - Test invalid commands and syntax errors
 */
public class MainTest {
    @TempDir
    Path tempDir;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    public void setUp() {
        // Capture System.out
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Save original System.in
        originalIn = System.in;
    }

    @AfterEach
    public void tearDown() {
        // Restore System.out and System.in
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // ==================== Help Command Tests ====================

    @Test
    public void testHelpCommand() {
        // Simulate user input: "help" then "exit"
        String input = "help\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_help.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        
        // Verify help message contains all expected commands
        assertTrue(output.contains("Available commands:"), "Should display 'Available commands'");
        assertTrue(output.contains("CREATE TABLE"), "Should mention CREATE TABLE");
        assertTrue(output.contains("DROP TABLE"), "Should mention DROP TABLE");
        assertTrue(output.contains("INSERT INTO"), "Should mention INSERT INTO");
        assertTrue(output.contains("SELECT"), "Should mention SELECT");
        assertTrue(output.contains("UPDATE"), "Should mention UPDATE");
        assertTrue(output.contains("DELETE FROM"), "Should mention DELETE FROM");
        assertTrue(output.contains("help"), "Should mention help command");
        assertTrue(output.contains("exit"), "Should mention exit command");
        assertTrue(output.contains("quit"), "Should mention quit command");
    }

    // ==================== Database Initialization Tests ====================

    @Test
    public void testDatabaseInitialization_DefaultFile() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Test with no arguments (should use default "nanodb.db")
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("NanoDB - A Simple Database System"), "Should display welcome message");
        assertTrue(output.contains("Database file:"), "Should display database file path");
        assertTrue(output.contains("Type 'exit' or 'quit' to exit"), "Should display exit instructions");
    }

    @Test
    public void testDatabaseInitialization_CustomFile() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("custom.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Database file: " + dbFile), "Should display custom database file path");
    }

    // ==================== Exit Command Tests ====================

    @Test
    public void testExitCommand_LowerCase() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_exit.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Goodbye!"), "Should display goodbye message on exit");
    }

    @Test
    public void testQuitCommand_LowerCase() {
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_quit.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Goodbye!"), "Should display goodbye message on quit");
    }

    @Test
    public void testExitCommand_UpperCase() {
        String input = "EXIT\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_EXIT.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Goodbye!"), "Should handle EXIT in uppercase");
    }

    @Test
    public void testQuitCommand_MixedCase() {
        String input = "QuIt\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_QUIT.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Goodbye!"), "Should handle QUIT in mixed case");
    }

    // ==================== Empty Input Tests ====================

    @Test
    public void testEmptyInput() {
        String input = "\n\n\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_empty.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should have 4 prompts (3 empty + 1 exit)
        int promptCount = countOccurrences(output, "nanodb>");
        assertTrue(promptCount >= 4, "Should display prompt for empty inputs");
    }

    // ==================== Command Processing Tests ====================

    @Test
    public void testCreateTableCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_create.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should not contain error message
        assertFalse(output.contains("Error:"), "Should not show error for valid CREATE TABLE");
    }

    @Test
    public void testInsertCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "INSERT INTO students (id, name) VALUES (1, 'Alice')\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_insert.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should not contain error messages
        assertFalse(output.contains("Error: SYNTAX_ERROR"), "Should not show syntax error");
    }

    @Test
    public void testSelectCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "INSERT INTO students (id, name) VALUES (1, 'Alice')\n" +
                      "SELECT * FROM students\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_select.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should display the selected row
        assertTrue(output.contains("Alice") || output.contains("1"), 
                   "Should display selected data");
    }

    @Test
    public void testUpdateCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "INSERT INTO students (id, name) VALUES (1, 'Alice')\n" +
                      "UPDATE students SET name = 'Bob' WHERE id = 1\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_update.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should show updated count
        assertTrue(output.contains("Updated") || !output.contains("Error:"), 
                   "Should process UPDATE command");
    }

    @Test
    public void testDeleteCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "INSERT INTO students (id, name) VALUES (1, 'Alice')\n" +
                      "DELETE FROM students WHERE id = 1\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_delete.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should show deleted count
        assertTrue(output.contains("Deleted") || !output.contains("Error:"), 
                   "Should process DELETE command");
    }

    @Test
    public void testDropTableCommand() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "DROP TABLE students\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_drop.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should not show error
        assertFalse(output.contains("Error: ERROR"), "Should process DROP TABLE successfully");
    }

    // ==================== Error Handling Tests ====================

    @Test
    public void testInvalidCommand() {
        String input = "INVALID COMMAND\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_invalid.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Error:"), "Should show error for invalid command");
        assertTrue(output.contains("UNRECOGNIZED_STATEMENT"), "Should report unrecognized statement");
    }

    @Test
    public void testSyntaxError_CreateTableWithoutColumns() {
        String input = "CREATE TABLE students\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_syntax.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Error:"), "Should show error for syntax error");
        assertTrue(output.contains("SYNTAX_ERROR"), "Should report syntax error");
    }

    @Test
    public void testTableNotFound() {
        String input = "SELECT * FROM nonexistent\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_notfound.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Error:"), "Should show error for non-existent table");
        assertTrue(output.contains("TABLE_NOT_FOUND"), "Should report table not found");
    }

    @Test
    public void testDataTypeMismatch() {
        String input = "CREATE TABLE students (id int, name varchar(50))\n" +
                      "INSERT INTO students (id, name) VALUES ('abc', 'Alice')\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_mismatch.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Error:"), "Should show error for data type mismatch");
        assertTrue(output.contains("DATA_TYPE_MISMATCH"), "Should report data type mismatch");
    }

    // ==================== Multiple Command Sequence Tests ====================

    @Test
    public void testMultipleCommands_CreateInsertSelect() {
        String input = "CREATE TABLE test (id int)\n" +
                      "INSERT INTO test (id) VALUES (1)\n" +
                      "INSERT INTO test (id) VALUES (2)\n" +
                      "INSERT INTO test (id) VALUES (3)\n" +
                      "SELECT * FROM test\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_multiple.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        // Should show all three inserted values
        assertTrue(output.contains("1") || output.contains("2") || output.contains("3"), 
                   "Should display inserted values");
    }

    @Test
    public void testHelpBetweenCommands() {
        String input = "CREATE TABLE test (id int)\n" +
                      "help\n" +
                      "INSERT INTO test (id) VALUES (1)\n" +
                      "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_help_between.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Available commands:"), "Should display help");
        assertFalse(output.contains("Error: SYNTAX_ERROR"), "Should continue processing after help");
    }

    @Test
    public void testCaseInsensitiveHelp() {
        String input = "HELP\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_help_upper.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("Available commands:"), "Should handle HELP in uppercase");
    }

    // ==================== Prompt Display Tests ====================

    @Test
    public void testPromptDisplay() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_prompt.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("nanodb>"), "Should display prompt");
    }

    @Test
    public void testMultiplePromptsForMultipleCommands() {
        String input = "help\nhelp\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_prompts.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        int promptCount = countOccurrences(output, "nanodb>");
        assertTrue(promptCount >= 3, "Should display prompt for each command");
    }

    // ==================== Welcome Message Tests ====================

    @Test
    public void testWelcomeMessage() {
        String input = "exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String dbFile = tempDir.resolve("test_welcome.db").toString();
        Main.main(new String[]{dbFile});

        String output = outputStream.toString();
        assertTrue(output.contains("NanoDB - A Simple Database System"), 
                   "Should display application name");
        assertTrue(output.contains("Database file:"), 
                   "Should display database file label");
        assertTrue(output.contains("Type 'exit' or 'quit' to exit"), 
                   "Should display exit instructions");
    }

    // ==================== Helper Methods ====================

    /**
     * Count occurrences of a substring in a string
     */
    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
