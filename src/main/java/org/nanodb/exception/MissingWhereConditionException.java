package org.nanodb.exception;

public class MissingWhereConditionException extends NanoDBException {
    public MissingWhereConditionException(String message) {
        super(message);
    }
}
