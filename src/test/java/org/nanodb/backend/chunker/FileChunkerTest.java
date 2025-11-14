package org.nanodb.backend.chunker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Since we are working with the same file, the tests cannot be parallel
@Execution(ExecutionMode.CONCURRENT)
public class FileChunkerTest extends ChunkerTest {
    private static String testfile = "test.ndb";
    private int chunkSize;
    private static int maxChunks = 4096;

    @Override
    protected Chunker createChunker(int chunkSize) {
        try {
            this.chunkSize = chunkSize;
            return new FileChunker(testfile, chunkSize, maxChunks);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void closeChunker() {
        try {
            ((FileChunker)this.chunker).close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
        try {
            ((FileChunker)this.chunker).close();
            Files.delete(Paths.get(testfile));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLoadExistingBitmap() {
        try {
            int chunkId = chunker.createNewChunk();
            String message = "Hello World!";
            chunker.updateChunk(chunkId, 0, message.getBytes(StandardCharsets.US_ASCII));

            closeChunker();

            this.chunker = new FileChunker(testfile, chunkSize, maxChunks);
            byte[] chunk = chunker.getChunk(chunkId);
            String newMessage = new String(chunk, 0, message.length(), StandardCharsets.US_ASCII);

            assertEquals(message, newMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
