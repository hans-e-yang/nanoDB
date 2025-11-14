package org.nanodb.backend.chunker;

public class SimpleMapChunkerTest extends ChunkerTest {
    @Override
    protected Chunker createChunker(int chunkSize) {
        return new SimpleMapChunker(chunkSize);
    }

}
