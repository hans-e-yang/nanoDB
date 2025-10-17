package org.nanodb;

import org.nanodb.db.*;
import org.nanodb.statement.*;
import org.nanodb.executor.*;
import org.nanodb.chunker.FileBasedChunker;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        FileBasedChunker chunker = new FileBasedChunker(4096, "DB.db");
        Db db = new FileBasedDb(chunker);
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("db > ");
                String line = scanner.nextLine().trim();
                if (line.equals(".exit")) {
                    break;
                }
                Statement statement = new Statement();
                PrepareResult prepare = StatementParser.prepareStatement(line, statement);
                if (prepare != PrepareResult.SUCCESS) {
                    System.out.println("Syntax error: " + prepare);
                    continue;
                }
                try {
                    ExecuteResult execute = Executor.execute(statement, db);
                    if (execute == ExecuteResult.SUCCESS) {
                        System.out.println("Executed.");
                    } else if (execute == ExecuteResult.DUPLICATE_KEY) {
                        System.out.println("Duplicate key.");
                    } else if (execute == ExecuteResult.TABLE_FULL) {
                        System.out.println("Table full.");
                    } else if (execute == ExecuteResult.TABLE_NOT_FOUND) {
                        System.out.println("Table not found.");
                    } else if (execute == ExecuteResult.COLUMN_NOT_FOUND) {
                        System.out.println("Column not found.");
                    } else {
                        System.out.println("Error.");
                    }
                } catch (IOException e) {
                    System.out.println("IO error: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } finally {
            scanner.close();
            if (chunker != null) {
                chunker.close();
            }
        }
    }
}