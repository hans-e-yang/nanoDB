package org.nanodb.backend.chunker;

public class ChunkDataTooLargeException extends RuntimeException {
    public ChunkDataTooLargeException(String message) {super(message);}
}
