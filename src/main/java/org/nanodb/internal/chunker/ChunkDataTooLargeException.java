package org.nanodb.internal.chunker;

public class ChunkDataTooLargeException extends RuntimeException {
    public ChunkDataTooLargeException(String message) {super(message);}
}
