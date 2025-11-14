package org.nanodb.backend.conditionVisitors;

public class InvalidColumnNameException extends RuntimeException {
    public InvalidColumnNameException(String message) {
        super(message);
    }
}
