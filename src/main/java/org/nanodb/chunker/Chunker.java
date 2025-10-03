package org.nanodb.chunker;

import java.util.Optional;

public abstract class Chunker {
    protected int chunkSize;

    public Chunker(int chunkSize) {this.chunkSize = chunkSize;}
    /**
     * Gets a fixed sized chunk based on its ID.
     * It is the responsibility of the caller to store the ID, except the initial ID.
     * @param id The ID of the chunk.
     * @throws InvalidChunkIdException ID of Chunk doesn't exist or is deleted
     * @return Contents of the chunk
     */

    public abstract byte[] getChunk(int id) throws InvalidChunkIdException;
    /**
     * Gets the initial chunk.
     * It is intended for the caller to store IDs of other chunk inside this chunk
     * @return Contents of the chunk
     */

    public abstract byte[] getInitialChunk();
    /**
     * Creates a new empty chunk
     * @return The ID of the new chunk
     */

    public abstract int createNewChunk();
    /**
     * Deletes an existing chunk
     * @throws InvalidChunkIdException ID of chunk doesn't exist or is deleted
     * @param id The ID of the chunk to delete
     */

    public abstract void deleteChunk(int id) throws InvalidChunkIdException;
    /**
     * Updates the data inside the chunk.
     * Will overwrite any existing data inside the chunk.
     * @throws InvalidChunkIdException ID of chunk doesn't exist or is deleted
     * @throws ChunkPositionOutOfBoundsException StartPosition is outside of chunk bounds
     * @throws ChunkDataTooLargeException Data given writes over the chunk end
     * @param id The ID of the chunk to update
     * @param startPosition 0-indexed byte position to start writing the data
     * @param data Data to write into the chunk
     */

    public abstract void updateChunk(int id, int startPosition, byte[] data)
            throws
            InvalidChunkIdException,
            ChunkPositionOutOfBoundsException,
            ChunkDataTooLargeException
    ;
}
