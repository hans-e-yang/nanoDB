package org.nanodb.internal.chunker;

import org.nanodb.internal.NanoDBException;

public class ChunkDataTooLargeException extends NanoDBException {
    public ChunkDataTooLargeException(String message) {super(message);}
}
