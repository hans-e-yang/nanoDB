package org.nanodb.chunker;

public class SimpleMapChunkerTest extends ChunkerTest {
    @Override
    protected Chunker createChunker(int chunkSize) {
        return new SimpleMapChunker(chunkSize);
    }
}
