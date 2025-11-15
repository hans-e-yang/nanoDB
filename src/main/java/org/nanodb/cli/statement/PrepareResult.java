package org.nanodb.cli.statement;

public enum PrepareResult {
    SUCCESS,
    SYNTAX_ERROR,
    NEGATIVE_ID,
    STRING_TOO_LONG,
    UNRECOGNIZED_STATEMENT
}