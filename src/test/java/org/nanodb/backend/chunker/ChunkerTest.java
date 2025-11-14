package org.nanodb.backend.chunker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nanodb.backend.chunker.ChunkDataTooLargeException;
import org.nanodb.backend.chunker.ChunkPositionOutOfBoundsException;
import org.nanodb.backend.chunker.Chunker;
import org.nanodb.backend.chunker.InvalidChunkIdException;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ChunkerTest {
    protected Chunker chunker;

    protected abstract Chunker createChunker(int chunkSize);
    private final int chunkSize = 4096;

    @BeforeEach
    public void setUp() {
        this.chunker = createChunker(chunkSize);
    }

    // Any teardown necessary for individual implementations
    @AfterEach
    public void tearDown() {}

    @Test
    public void testCreateNewChunk() {
        // Should not throw.
        int chunkId = chunker.createNewChunk();
        chunker.getChunk(chunkId);
    }

    @Test
    public void testGetInitialChunk() {
        byte[] initialChunk = chunker.getInitialChunk();
        assertNotNull(initialChunk);
    }

    @Test
    public void testGetChunk() throws InvalidChunkIdException {
        int chunkId = chunker.createNewChunk();
        byte[] chunkData = chunker.getChunk(chunkId);
        assertNotNull(chunkData);
        assertEquals(chunkSize, chunkData.length);
    }

    @Test
    public void testGetChunkInvalidId() {
        assertThrows(InvalidChunkIdException.class, () -> {
            chunker.getChunk(999); // Non-existing ID
        });
    }

    @Test
    public void testDeleteChunk() throws InvalidChunkIdException {
        int chunkId = chunker.createNewChunk();
        chunker.deleteChunk(chunkId); // Should not throw an exception
        assertThrows(InvalidChunkIdException.class, () -> {
            chunker.getChunk(chunkId); // Now it should throw an exception
        });
    }

    @Test
    public void testUpdateChunk() throws InvalidChunkIdException, ChunkPositionOutOfBoundsException, ChunkDataTooLargeException {
        int chunkId = chunker.createNewChunk();
        byte[] data = new byte[this.chunkSize];
        new Random().nextBytes(data);
        chunker.updateChunk(chunkId, 0, data); // Should not throw an exception
        byte[] chunkData = chunker.getChunk(chunkId);
        assertArrayEquals(data, chunkData);
    }

    @Test
    public void testUpdateChunkInvalidId() {
        assertThrows(InvalidChunkIdException.class, () -> {
            chunker.updateChunk(999, 0, new byte[]{1, 2}); // Non-existing ID
        });
    }

    @Test
    public void testUpdateChunkPositionOutOfBounds() throws InvalidChunkIdException {
        int chunkId = chunker.createNewChunk();
        assertThrows(ChunkPositionOutOfBoundsException.class, () -> {
            chunker.updateChunk(chunkId, chunkSize+1, new byte[]{1}); // Start position is out of bounds
        });
    }

    @Test
    public void testUpdateChunkDataTooLarge() throws InvalidChunkIdException {
        int chunkId = chunker.createNewChunk();
        chunker.updateChunk(chunkId, 0, new byte[10]); // Create a chunk with size 10
        assertThrows(ChunkDataTooLargeException.class, () -> {
            chunker.updateChunk(chunkId, 5, new byte[chunkSize+1]); // Data exceeds chunk size
        });
    }

    @Test
    public void testWriteAndRead() {
        int chunkId = chunker.createNewChunk();
        String message = "Hello World!";
        chunker.updateChunk(chunkId, 0, message.getBytes(StandardCharsets.US_ASCII));

        byte[] chunk = chunker.getChunk(chunkId);
        String newMessage = new String(chunk, 0, message.length(), StandardCharsets.US_ASCII);

        assertEquals(message, newMessage);
    }

}
