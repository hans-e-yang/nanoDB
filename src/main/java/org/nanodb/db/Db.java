package org.nanodb.db;

import org.nanodb.chunker.Chunker;
import org.nanodb.condition.Condition;
import org.nanodb.primitives.StructType;

import java.util.List;
import java.util.Map;

// Should represent the data structure representing the database
public abstract class Db {
    protected Chunker chunker;
    
    public Db(Chunker chunker) { 
        this.chunker = chunker; 
    }
    
    public abstract boolean createTable(String tableName, StructType schema);
    public abstract boolean dropTable(String tableName);
    public abstract boolean insert(String tableName, Map<String, Object> row);
    public abstract List<Map<String, Object>> select(String tableName, Condition condition);
    public abstract int delete(String tableName, Condition condition);
    public abstract int update(String tableName, Map<String, Object> setValues, Condition condition);
    public abstract boolean tableExists(String tableName);
    public abstract StructType getTableSchema(String tableName);
}
