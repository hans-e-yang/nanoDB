package org.nanodb.orm;

import org.nanodb.internal.chunker.Chunker;
import org.nanodb.internal.chunker.FileChunker;
import org.nanodb.internal.db.Database;
import org.nanodb.internal.db.Db;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.internal.primitives.visitor.DataTypeComparer;
import org.nanodb.internal.tableFactory.LinkedListTableFactory;
import org.nanodb.internal.tableFactory.TableFactory;
import org.nanodb.orm.annotations.Ignore;
import org.nanodb.orm.internal.ClassToDataType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class NanoDb implements AutoCloseable {

    // Default settings of this DB
    private static final int CHUNK_SIZE = 32768; // 32Kib chunks
    private static final int MAX_CHUNKS = 32768; // 32768 bits of chunks -> 4kb used for bitmap
    // Max db size = 2 * 30 == 1gb

    private Chunker chunker;
    private TableFactory tableFactory;
    private Db db;

    // reflection here
    public NanoDb(String filename) throws IOException, IllegalAccessException {
        // chunker etcetc here.
        chunker = new FileChunker(filename, CHUNK_SIZE, MAX_CHUNKS);
        try {
            tableFactory = new LinkedListTableFactory(chunker);
            db = new Database(tableFactory);

            // If db not initialized, initialize db?
            Class<? extends NanoDb> dbClass = this.getClass();

            // Check if file is initialized?
            Set<String> tableNames = new HashSet<>();

            // Auto Migrations ====
            // Create any tables that doesn't exist in file or is different from the tables we have
            for (Field field : dbClass.getDeclaredFields()) {
                // Ignore any fields annotated by @NanoDbIgnore
                if (field.isAnnotationPresent(Ignore.class)) continue;
                // Ignore any fields that isn't type Table
                if (!field.getType().equals(Table.class)) continue;

                // add into tablenames list
                String tableName = field.getName();
                tableNames.add(tableName);


                // Delete existing table, but schema is different from our schema
                ParameterizedType type = (ParameterizedType) field.getGenericType();
                Class<?> tableType = (Class<?>)type.getActualTypeArguments()[0];
                if (!BaseModel.class.isAssignableFrom(tableType)) throw new UnsupportedOperationException("Table type must inherit from BaseModel");
                StructType dataType = ClassToDataType.getDataType((Class<? extends BaseModel>)tableType);

                // DROP TABLE IF SCHEMA IS DIFFERENT
                if (db.tableExists(tableName) && !DataTypeComparer.compare(dataType, db.getTableSchema(tableName))) db.dropTable(tableName);

                if (!db.tableExists(tableName)) db.createTable(field.getName(), dataType);

                // Get the tables, and push to the Table<T> wrappers, to check the individual fields
                field.setAccessible(true);
                field.set(this, new Table<>(db.getTable(tableName), tableType));
            }

            // DROP ALL TABLE THAT DOESN'T EXIST IN CLASS
            // Delete any table that doesn't exist in class
            for (String tableName : db.getAllTables()) {
                if (!tableNames.contains(tableName)) db.dropTable(tableName);
            }
            // Migrations ====
        } catch (Exception e) {
            // Close the chunker to release file handles if an exception occurs during setup
            try {
                chunker.close();
            } catch (IOException ignored) {
                // Suppress any exception during cleanup
            }
            throw e;
        }
    }

    /**
     * Closes this database and releases any associated resources.
     * This method should be called when the database is no longer needed.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (chunker != null) {
            chunker.close();
        }
    }

    // Wrap DB!!!
}
