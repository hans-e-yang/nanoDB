package org.nanodb.backend.condition.visitor;

public class InvalidColumnDataTypeException extends RuntimeException {
    public InvalidColumnDataTypeException(String message) {
        super(message);
    }
}
