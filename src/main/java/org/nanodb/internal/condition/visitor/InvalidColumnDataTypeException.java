package org.nanodb.internal.condition.visitor;

import org.nanodb.internal.exception.NanoDBException;

public class InvalidColumnDataTypeException extends NanoDBException {
    public InvalidColumnDataTypeException(String message) {
        super(message);
    }
}
