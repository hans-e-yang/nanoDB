package org.nanodb.internal.condition.visitor;

import org.nanodb.internal.exception.NanoDBException;

public class InvalidColumnNameException extends NanoDBException {
    public InvalidColumnNameException(String message) {
        super(message);
    }
}
