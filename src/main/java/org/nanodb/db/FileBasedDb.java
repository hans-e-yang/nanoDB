package org.nanodb.db;

import org.nanodb.chunker.FileBasedChunker;
import org.nanodb.chunker.InvalidChunkIdException;
import org.nanodb.statement.condition.Condition;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * File-based database implementation that persists data to DB.db
 */
public class FileBasedDb extends Db {
    private FileBasedChunker fileChunker;
    private Map<String, Table> tables = new HashMap<>();
    private Map<String, Integer> tableChunkMap = new HashMap<>(); // Maps table name to chunk ID
    private int nextTableChunkId = 1; // Start from 1, 0 is reserved for metadata

    public FileBasedDb(FileBasedChunker chunker) {
        super(chunker);
        this.fileChunker = chunker;
        loadTables();
    }

    /**
     * Load table metadata from the initial chunk
     */
    private void loadTables() {
        try {
            byte[] initialChunk = fileChunker.getInitialChunk();
            // Parse table metadata from initial chunk
            ByteBuffer buffer = ByteBuffer.wrap(initialChunk);
            int tableCount = buffer.getInt();
            
            for (int i = 0; i < tableCount; i++) {
                // Read table name length
                int nameLength = buffer.getInt();
                byte[] nameBytes = new byte[nameLength];
                buffer.get(nameBytes);
                String tableName = new String(nameBytes);
                
                // Read table data chunk ID
                int tableChunkId = buffer.getInt();
                
                // Load the table's rows
                Table table = loadTableRows(tableName, tableChunkId);
                tables.put(tableName, table);
                tableChunkMap.put(tableName, tableChunkId);
                if (tableChunkId >= nextTableChunkId) {
                    nextTableChunkId = tableChunkId + 1;
                }
            }
        } catch (InvalidChunkIdException e) {
            // No existing metadata, start fresh
            tables.clear();
        }
    }

    /**
     * Load all rows for a specific table from its chunk
     */
    private Table loadTableRows(String tableName, int chunkId) throws InvalidChunkIdException {
        Table table = new Table(tableName);
        byte[] chunk = fileChunker.getChunk(chunkId);
        ByteBuffer buffer = ByteBuffer.wrap(chunk);
        
        int rowCount = buffer.getInt();
        for (int i = 0; i < rowCount; i++) {
            Row row = deserializeRow(buffer);
            if (row != null) {
                table.insert(row);
            }
        }
        
        return table;
    }

    /**
     * Serialize table metadata to initial chunk
     */
    private void saveTableMetadata() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(fileChunker.getChunkSize());
            buffer.putInt(tables.size()); // Number of tables
            
            for (Map.Entry<String, Table> entry : tables.entrySet()) {
                String tableName = entry.getKey();
                Integer chunkId = tableChunkMap.get(tableName);
                if (chunkId == null) {
                    chunkId = nextTableChunkId++;
                    tableChunkMap.put(tableName, chunkId);
                }
                
                buffer.putInt(tableName.length());
                buffer.put(tableName.getBytes());
                buffer.putInt(chunkId);
            }
            
            fileChunker.updateChunk(0, 0, buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error saving table metadata: " + e.getMessage());
        }
    }

    /**
     * Serialize a Row object to bytes
     */
    private byte[] serializeRow(Row row) {
        ByteBuffer buffer = ByteBuffer.allocate(Row.ROW_SIZE);
        buffer.putInt(row.id);
        byte[] nameBytes = row.courseName.getBytes();
        buffer.put(nameBytes);
        buffer.position(4 + Row.COURSE_NAME_SIZE);
        byte[] codeBytes = row.courseCode.getBytes();
        buffer.put(codeBytes);
        return buffer.array();
    }

    /**
     * Deserialize a Row object from bytes
     */
    private Row deserializeRow(ByteBuffer buffer) {
        if (buffer.remaining() < Row.ROW_SIZE) {
            return null;
        }
        int id = buffer.getInt();
        byte[] nameBytes = new byte[Row.COURSE_NAME_SIZE];
        buffer.get(nameBytes);
        String courseName = new String(nameBytes).trim();
        byte[] codeBytes = new byte[Row.COURSE_CODE_SIZE];
        buffer.get(codeBytes);
        String courseCode = new String(codeBytes).trim();
        
        return new Row(id, courseName, courseCode);
    }

    @Override
    public boolean createTable(String tableName) {
        if (tables.containsKey(tableName)) {
            return false;
        }
        try {
            Table table = new Table(tableName);
            tables.put(tableName, table);
            // Allocate a chunk for this table immediately
            int chunkId = fileChunker.createNewChunk();
            tableChunkMap.put(tableName, chunkId);
            saveTableMetadata();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error creating table: " + e.getMessage());
        }
    }

    @Override
    public boolean dropTable(String tableName) {
        if (!tables.containsKey(tableName)) {
            return false;
        }
        tables.remove(tableName);
        saveTableMetadata();
        return true;
    }

    @Override
    public boolean insert(String tableName, Row row) {
        Table table = tables.get(tableName);
        if (table == null) {
            return false;
        }
        boolean success = table.insert(row);
        if (success) {
            persistTable(tableName);
        }
        return success;
    }

    @Override
    public List<Row> select(String tableName, Condition condition) {
        Table table = tables.get(tableName);
        if (table == null) {
            return new ArrayList<>();
        }
        return table.select(condition);
    }

    @Override
    public int delete(String tableName, Condition condition) {
        Table table = tables.get(tableName);
        if (table == null) {
            return 0;
        }
        int count = table.delete(condition);
        if (count > 0) {
            persistTable(tableName);
        }
        return count;
    }

    @Override
    public int update(String tableName, Map<String, String> setValues, Condition condition) {
        Table table = tables.get(tableName);
        if (table == null) {
            return 0;
        }
        int count = table.update(setValues, condition);
        if (count > 0) {
            persistTable(tableName);
        }
        return count;
    }

    @Override
    public boolean tableExists(String tableName) {
        return tables.containsKey(tableName);
    }

    /**
     * Persist a table's data to disk
     */
    private void persistTable(String tableName) {
        try {
            Table table = tables.get(tableName);
            if (table == null) {
                return;
            }
            
            // Get or allocate a chunk for this table
            Integer chunkId = tableChunkMap.get(tableName);
            if (chunkId == null) {
                // Allocate a new chunk for this table
                chunkId = fileChunker.createNewChunk();
                tableChunkMap.put(tableName, chunkId);
            }
            
            List<Row> rows = table.getAllRows();
            ByteBuffer buffer = ByteBuffer.allocate(fileChunker.getChunkSize());
            buffer.putInt(rows.size()); // Row count
            
            for (Row row : rows) {
                byte[] rowData = serializeRow(row);
                if (buffer.remaining() >= rowData.length) {
                    buffer.put(rowData);
                }
            }
            
            fileChunker.updateChunk(chunkId, 0, buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error persisting table: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        fileChunker.close();
    }
}
