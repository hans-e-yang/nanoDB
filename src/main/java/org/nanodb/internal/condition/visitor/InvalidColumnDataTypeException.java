package org.nanodb.internal.condition.visitor;

public class InvalidColumnDataTypeException extends RuntimeException {
    public InvalidColumnDataTypeException(String message) {
        super(message);
    }
}
