package org.nanodb.internal.chunker;

import org.nanodb.internal.exception.NanoDBException;

public class NoMoreChunksAvailableException extends NanoDBException {
    public NoMoreChunksAvailableException(String message) {
        super(message);
    }
}
