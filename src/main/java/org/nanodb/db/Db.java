package org.nanodb.db;

import org.nanodb.chunker.Chunker;
import org.nanodb.primitives.StructType;
import org.nanodb.table.Table;

// Should represent the data structure representing the database
public abstract class Db {
    protected Chunker chunker;


    // Constructor to initialize the database with a specified chunker.
    //
    // @param chunker the chunker used for handling data storage and management
    public Db(Chunker chunker) { 
        this.chunker = chunker; 
    }

    // Creates a new table in the database with the given name and schema.
    //
    // @param tableName the name of the new table
    // @param schema the structure defining the schema of the table
    // @return true if the table is created successfully, false otherwise
    public abstract boolean createTable(String tableName, StructType schema);
    // Drops an existing table from the database.
    //
    // @param tableName the name of the table to drop
    // @return true if the table is dropped successfully, false otherwise
    public abstract boolean dropTable(String tableName);

    // Retrieves the table object for the specified table name.
    //
    // @param tableName the name of the table to retrieve
    // @return the Table object representing the specified table
    public abstract Table getTable(String tableName);

    // Retrieves the schema of the specified table.
    //
    // @param tableName the name of the table whose schema is to be retrieved
    // @return the StructType representing the schema of the specified table
    public abstract StructType getTableSchema(String tableName);
}
