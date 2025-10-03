package org.nanodb.chunker;

import java.util.HashMap;
import java.util.Map;

public class SimpleMapChunker extends Chunker {
    private final Map<Integer, byte[]> chunks = new HashMap<>();
    private int nextId = 0;

    public SimpleMapChunker(int chunkSize) {
        super(chunkSize);
    }

    @Override
    public byte[] getChunk(int id) throws InvalidChunkIdException {
        if (!chunks.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        return chunks.get(id);
    }

    @Override
    public byte[] getInitialChunk() {
        if (nextId == 0) {
            createNewChunk();
        }
        return getChunk(0); // This will return the initial chunk's data
    }

    @Override
    public int createNewChunk() {
        chunks.put(nextId, new byte[chunkSize]); // Create an empty chunk
        return nextId++;
    }

    @Override
    public void deleteChunk(int id) throws InvalidChunkIdException {
        if (!chunks.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        chunks.remove(id); // Delete the chunk
    }

    @Override
    public void updateChunk(int id, int startPosition, byte[] data)
            throws InvalidChunkIdException, ChunkPositionOutOfBoundsException, ChunkDataTooLargeException {
        if (!chunks.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        byte[] chunk = chunks.get(id);
        if (startPosition < 0 || startPosition > chunk.length) {
            throw new ChunkPositionOutOfBoundsException("Start position is outside of chunk bounds: " );
        }
        if (startPosition + data.length > chunk.length) {
            throw new ChunkDataTooLargeException("Data given writes over the chunk end");
        }
        System.arraycopy(data, 0, chunk, startPosition, data.length); // Update the chunk data
    }
}
