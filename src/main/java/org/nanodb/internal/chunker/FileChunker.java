package org.nanodb.internal.chunker;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class FileChunker extends Chunker {
    private RandomAccessFile file;
    private BitSet chunkMap; // Bitmap to track allocated chunks

    public FileChunker(String filePath, int chunkSize, int maxChunks) throws IOException {
        super(chunkSize);

        // Initialize the file
        file = new RandomAccessFile(filePath, "rw");

        // Calculate the maximum number of chunks based on file size
        this.chunkMap = new BitSet(maxChunks);
        if (file.length() < getChunkMapSizeBytes()) {
            initializeBitmap();
        } else {
            loadBitmap();
        }
    }

    private int getChunkMapSizeBytes() {return (chunkMap.size()/8) + 1;}

    private void initializeBitmap() throws IOException {
        writeBitmapToFile();
        // Create the initial chunk 0
        // Create new chunk always gets the earliest chunk, so this will create chunk 0
        createNewChunk();
    }

    private void loadBitmap() throws IOException {
        file.seek(0); // Start at the beginning of the file
        byte[] bitmapData = new byte[getChunkMapSizeBytes()]; // Calculate byte size for bitmap
        file.readFully(bitmapData);
        for (int i = 0; i < chunkMap.size(); i++) {
            chunkMap.set(i, (bitmapData[i / 8] & (1 << (i % 8))) != 0);
        }
    }

    private void writeBitmapToFile() throws IOException {
        file.seek(0); // Write bitmap at the beginning of the file
        byte[] bitmapData = new byte[getChunkMapSizeBytes()];
        for (int i = 0; i < chunkMap.size(); i++) {
            if (chunkMap.get(i)) {
                bitmapData[i / 8] |= (1 << (i % 8));
            }
        }
        file.write(bitmapData);
    }

    @Override
    public byte[] getChunk(int id) throws InvalidChunkIdException {
        if (id < 0 || id >= chunkMap.size() || !chunkMap.get(id)) {
            throw new InvalidChunkIdException("ID of Chunk doesn't exist or is deleted.");
        }

        byte[] chunkData = new byte[chunkSize];
        try {
            file.seek(getChunkOffset(id));
            file.readFully(chunkData);
        } catch (IOException e) {
            e.printStackTrace(); // Handle IOException appropriately
        }
        return chunkData;
    }

    private long getChunkOffset(int id) {
        return getChunkMapSizeBytes() + id * chunkSize; // Offset considering the bitmap header
    }

    @Override
    public int getInitialChunkId() {
        return 0; // Assuming the initial chunk is always ID 0
    }

    @Override
    public int createNewChunk() {
        for (int i = 0; i < chunkMap.size(); i++) {
            if (!chunkMap.get(i)) { // Find a free chunk
                chunkMap.set(i); // Mark this chunk as used
                try {
                    byte[] emptyChunk = new byte[chunkSize];
                    file.seek(getChunkOffset(i));
                    file.write(emptyChunk);
                    writeBitmapToFile(); // Update the bitmap in the file
                } catch (IOException e) {
                    e.printStackTrace(); // Handle IOException appropriately
                }
                return i; // Return the ID of the new chunk
            }
        }
        throw new RuntimeException("No more chunks available."); // No available space
    }

    @Override
    public void deleteChunk(int id) throws InvalidChunkIdException {
        if (id < 0 || id >= chunkMap.size() || !chunkMap.get(id)) {
            throw new InvalidChunkIdException("ID of chunk doesn't exist or is deleted.");
        }
        chunkMap.clear(id); // Mark the chunk as free

        try {
            writeBitmapToFile(); // Update the bitmap in the file
        } catch (IOException e) {
            e.printStackTrace(); // Handle IOException appropriately
        }
    }

    @Override
    public void updateChunk(int id, int startPosition, byte[] data)
            throws InvalidChunkIdException, ChunkPositionOutOfBoundsException, ChunkDataTooLargeException {

        if (id < 0 || id >= chunkMap.size() || !chunkMap.get(id)) {
            throw new InvalidChunkIdException("ID of chunk doesn't exist or is deleted.");
        }
        if (startPosition < 0 || startPosition >= chunkSize) {
            throw new ChunkPositionOutOfBoundsException("StartPosition is outside of chunk bounds.");
        }
        if (startPosition + data.length > chunkSize) {
            throw new ChunkDataTooLargeException("Data given writes over the chunk end.");
        }

        try {
            file.seek(getChunkOffset(id) + startPosition);
            file.write(data);
        } catch (IOException e) {
            e.printStackTrace(); // Handle IOException appropriately
        }
    }

    // Close the RandomAccessFile when done
    @Override
    public void close() throws IOException {
        file.close();
    }
}