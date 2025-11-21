package org.nanodb.exception;

public class NanoDBException extends Exception {
    public NanoDBException(String message) {
        super(message);
    }

    public NanoDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
