package org.nanodb.db;

import org.nanodb.chunker.Chunker;
import org.nanodb.statement.condition.Condition;

import java.util.List;
import java.util.Map;

// Should represent the data structure representing the database
public abstract class Db {
    protected Chunker chunker;
    
    public Db(Chunker chunker) { 
        this.chunker = chunker; 
    }
    
    public abstract boolean createTable(String tableName);
    public abstract boolean dropTable(String tableName);
    public abstract boolean insert(String tableName, Row row);
    public abstract List<Row> select(String tableName, Condition condition);
    public abstract int delete(String tableName, Condition condition);
    public abstract int update(String tableName, Map<String, String> setValues, Condition condition);
    public abstract boolean tableExists(String tableName);
}
