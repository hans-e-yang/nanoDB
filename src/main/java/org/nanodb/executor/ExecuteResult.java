package org.nanodb.executor;

public enum ExecuteResult {
    SUCCESS,
    DUPLICATE_KEY,
    TABLE_FULL,
    ERROR
}