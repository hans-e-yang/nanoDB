package org.nanodb;

import org.nanodb.cli.executor.ExecuteResult;
import org.nanodb.cli.executor.Executor;
import org.nanodb.cli.statement.PrepareResult;
import org.nanodb.cli.statement.Statement;
import org.nanodb.cli.statement.StatementParser;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.db.Database;
import org.nanodb.internal.db.Db;
import org.nanodb.internal.tableFactory.LinkedListTableFactory;

import java.io.IOException;
import java.util.Scanner;

/**
 * NanoDB Main - Command Line Interface
 * Provides an interactive SQL-like shell for database operations
 */
public class Main {
    private static final String PROMPT = "nanodb> ";
    private static final String DB_FILE = "nanodb.db";
    private static final int CHUNK_SIZE = 4096;
    private static final int MAX_CHUNKS = 10000;

    public static void main(String[] args) {
        String dbFile = DB_FILE;
        
        // Allow custom database file from command line
        if (args.length > 0) {
            dbFile = args[0];
        }

        System.out.println("NanoDB - A Simple Database System");
        System.out.println("Database file: " + dbFile);
        System.out.println("Type 'exit' or 'quit' to exit");
        System.out.println();

        FileChunker chunker = null;
        try {
            chunker = new FileChunker(dbFile, CHUNK_SIZE, MAX_CHUNKS);
            LinkedListTableFactory factory = new LinkedListTableFactory(chunker);
            Db db = new Database(factory);
            
            runREPL(db);
            
        } catch (IOException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            System.exit(1);
        } finally {
            if (chunker != null) {
                try {
                    chunker.close();
                } catch (IOException e) {
                    System.err.println("Error closing database: " + e.getMessage());
                }
            }
        }
    }

    private static void runREPL(Db db) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print(PROMPT);
            
            if (!scanner.hasNextLine()) {
                break;
            }
            
            String input = scanner.nextLine().trim();
            
            // Handle exit commands
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }
            
            // Skip empty lines
            if (input.isEmpty()) {
                continue;
            }
            
            // Handle help command
            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            
            // Process SQL statement
            processStatement(input, db);
        }
        
        scanner.close();
    }

    private static void processStatement(String input, Db db) {
        Statement statement = new Statement();
        
        // Parse the statement
        PrepareResult prepareResult = StatementParser.prepareStatement(input, statement);
        
        if (prepareResult != PrepareResult.SUCCESS) {
            System.out.println("Error: " + prepareResult);
            return;
        }
        
        // Execute the statement
        try {
            ExecuteResult executeResult = Executor.execute(statement, db);
            
            if (executeResult != ExecuteResult.SUCCESS) {
                System.out.println("Error: " + executeResult);
            }
        } catch (IOException e) {
            System.out.println("Error executing statement: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  CREATE TABLE <name> (<column> <type>, ...)");
        System.out.println("    Types: int, varchar(<size>), bytes(<size>)");
        System.out.println("  DROP TABLE <name>");
        System.out.println("  INSERT INTO <table> (<columns>) VALUES (<values>)");
        System.out.println("  SELECT <columns|*> FROM <table> [WHERE <condition>] [LIMIT <n>]");
        System.out.println("  UPDATE <table> SET <column>=<value> [WHERE <condition>] [LIMIT <n>]");
        System.out.println("  DELETE FROM <table> [WHERE <condition>] [LIMIT <n>]");
        System.out.println();
        System.out.println("  help  - Show this help message");
        System.out.println("  exit  - Exit the program");
        System.out.println("  quit  - Exit the program");
    }
}