package org.nanodb.conditionVisitors;

public class InvalidColumnDataTypeException extends RuntimeException {
    public InvalidColumnDataTypeException(String message) {
        super(message);
    }
}
