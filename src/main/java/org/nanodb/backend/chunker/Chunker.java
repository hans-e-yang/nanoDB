package org.nanodb.backend.chunker;

public abstract class Chunker {
    protected int chunkSize;

    public Chunker(int chunkSize) {
        if (chunkSize < 4096) chunkSize = 4096;
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {return chunkSize;}
    /**
     * chunkSize will be at least 4kb, if given chunksize is < 4kb, it will be made into 4kb
     * Gets a fixed sized chunk based on its ID.
     * It is the responsibility of the caller to store the ID, except the initial ID.
     * @param id The ID of the chunk. - Must be positive, negative ID will return an empty array
     * @throws InvalidChunkIdException ID of Chunk doesn't exist or is deleted
     * @return Contents of the chunk
     */

    public abstract byte[] getChunk(int id) throws InvalidChunkIdException;
    /**
     * Gets the initial chunk.
     * It is intended for the caller to store IDs of other chunk inside this chunk
     * @return Contents of the chunk
     */


    public abstract int getInitialChunkId();
    /**
     * Returns the first ID to use
     * @return The ID of the initial chunk
     */

    public byte[] getInitialChunk() {return getChunk(getInitialChunkId());};
    /**
     * Creates a new empty chunk
     * @return The Data of the initial chunk
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
