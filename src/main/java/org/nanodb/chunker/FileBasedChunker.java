package org.nanodb.chunker;

import java.io.*;
import java.util.*;

/**
 * File-based chunker that uses RandomAccessFile to persist data to disk
 */
public class FileBasedChunker extends Chunker {
    private RandomAccessFile raf;
    private final Map<Integer, Long> chunkOffsets = new HashMap<>();
    private int nextId = 0;
    private static final int METADATA_SIZE = 4; // 4 bytes for nextId

    public FileBasedChunker(int chunkSize, String filename) throws IOException {
        super(chunkSize);
        this.raf = new RandomAccessFile(filename, "rw");
        loadMetadata();
    }

    /**
     * Load metadata from the beginning of the file
     */
    private void loadMetadata() throws IOException {
        if (raf.length() == 0) {
            // New file, initialize with metadata
            raf.writeInt(0); // nextId = 0
            raf.seek(0);
            nextId = 0;
        } else {
            // Read existing metadata
            raf.seek(0);
            nextId = raf.readInt();
            // Rebuild chunk offset map
            long pos = METADATA_SIZE;
            for (int i = 0; i < nextId; i++) {
                chunkOffsets.put(i, pos);
                pos += chunkSize;
            }
        }
    }

    @Override
    public byte[] getChunk(int id) throws InvalidChunkIdException {
        if (!chunkOffsets.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        try {
            Long offset = chunkOffsets.get(id);
            raf.seek(offset);
            byte[] chunk = new byte[chunkSize];
            int bytesRead = raf.read(chunk);
            if (bytesRead != chunkSize) {
                throw new InvalidChunkIdException("Failed to read complete chunk");
            }
            return chunk;
        } catch (IOException e) {
            throw new InvalidChunkIdException("IO error reading chunk: " + e.getMessage());
        }
    }

    @Override
    public byte[] getInitialChunk() {
        try {
            if (nextId == 0) {
                createNewChunk();
            }
            return getChunk(0);
        } catch (InvalidChunkIdException e) {
            throw new RuntimeException("Failed to get initial chunk", e);
        }
    }

    @Override
    public int createNewChunk() {
        try {
            long offset = METADATA_SIZE + (long) nextId * chunkSize;
            chunkOffsets.put(nextId, offset);
            raf.seek(offset);
            byte[] emptyChunk = new byte[chunkSize];
            raf.write(emptyChunk);
            raf.seek(0);
            raf.writeInt(nextId + 1);
            return nextId++;
        } catch (IOException e) {
            throw new RuntimeException("IO error creating chunk: " + e.getMessage());
        }
    }

    @Override
    public void deleteChunk(int id) throws InvalidChunkIdException {
        if (!chunkOffsets.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        // Mark as deleted by removing from offset map
        chunkOffsets.remove(id);
    }

    @Override
    public void updateChunk(int id, int startPosition, byte[] data)
            throws InvalidChunkIdException, ChunkPositionOutOfBoundsException, ChunkDataTooLargeException {
        if (!chunkOffsets.containsKey(id)) {
            throw new InvalidChunkIdException("Chunk ID doesn't exist or is deleted");
        }
        if (startPosition < 0 || startPosition > chunkSize) {
            throw new ChunkPositionOutOfBoundsException("Start position is outside of chunk bounds");
        }
        if (startPosition + data.length > chunkSize) {
            throw new ChunkDataTooLargeException("Data given writes over the chunk end");
        }
        try {
            Long offset = chunkOffsets.get(id);
            raf.seek(offset + startPosition);
            raf.write(data);
        } catch (IOException e) {
            throw new RuntimeException("IO error updating chunk: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}
