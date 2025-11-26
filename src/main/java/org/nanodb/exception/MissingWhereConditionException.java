package org.nanodb.exception;

import org.nanodb.internal.NanoDBException;

public class MissingWhereConditionException extends NanoDBException {
    public MissingWhereConditionException(String message) {
        super(message);
    }
}
