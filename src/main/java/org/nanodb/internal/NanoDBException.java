package org.nanodb.internal;

public class NanoDBException extends RuntimeException {
    public NanoDBException(String message) {
        super(message);
    }

    public NanoDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
