package org.nanodb.internal.chunker;

import org.nanodb.internal.exception.NanoDBException;

public class InvalidChunkIdException extends NanoDBException {
    public InvalidChunkIdException(String message) {super(message);}
}
