package org.nanodb.internal.db;

import org.nanodb.internal.exception.NanoDBException;

public class TableFileCorruptedException extends NanoDBException {
    private static final String PREFIX = "Database file corrupted: ";
    public TableFileCorruptedException(String message) {
        super(PREFIX + message);
    }
}
