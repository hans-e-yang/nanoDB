package org.nanodb.internal.db;

import org.nanodb.internal.primitives.StructType;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.utils.Table;
import org.nanodb.internal.tableFactory.TableFactory;

import java.util.List;

// Should represent the data structure representing the database
// Better errors may need to be implemented
public abstract class Db {
    protected TableFactory tableFactory;

    // Constructor to initialize the database with a specified chunker.
    //
    // @param chunker the chunker used for handling data storage and management
    public Db(TableFactory factory) {
        this.tableFactory = factory;
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
    // @return the Table object representing the specified table or null if it doesn't exist
    public abstract InternalTable getTable(String tableName);

    // Retrieves all table names in the db
    public abstract List<String> getAllTables();

    // Retrieves the schema of the specified table.
    //
    // @param tableName the name of the table whose schema is to be retrieved
    // @return the StructType representing the schema of the specified table
    public abstract StructType getTableSchema(String tableName);

    public boolean tableExists(String tableName) { return this.getTable(tableName) != null;}
}
