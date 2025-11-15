package org.nanodb.cli.executor;

public enum ExecuteResult {
    SUCCESS,
    DUPLICATE_KEY,
    TABLE_FULL,
    COLUMN_NOT_FOUND,
    TABLE_NOT_FOUND,
    DATA_TYPE_MISMATCH,
    ERROR
}