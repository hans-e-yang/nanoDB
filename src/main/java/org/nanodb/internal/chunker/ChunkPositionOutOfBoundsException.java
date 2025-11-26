package org.nanodb.internal.chunker;

import org.nanodb.internal.exception.NanoDBException;

public class ChunkPositionOutOfBoundsException extends NanoDBException {
    public ChunkPositionOutOfBoundsException(String message) {super(message);}
}
