package org.nanodb.executor;

public enum ExecuteResult {
    SUCCESS,
    DUPLICATE_KEY,
    TABLE_FULL,
    COLUMN_NOT_FOUND,
    TABLE_NOT_FOUND,
    ERROR
}