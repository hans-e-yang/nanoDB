package org.nanodb.backend.condition.visitor;

public class InvalidColumnNameException extends RuntimeException {
    public InvalidColumnNameException(String message) {
        super(message);
    }
}
