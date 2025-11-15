package org.nanodb.backend.db;

public class TableFileCorruptedException extends RuntimeException {
    private static final String PREFIX = "Database file corrupted: ";
    public TableFileCorruptedException(String message) {
        super(PREFIX + message);
    }
}
