package org.nanodb.backend.table;

import org.nanodb.backend.chunker.Chunker;
import org.nanodb.backend.condition.BooleanCondition;
import org.nanodb.backend.condition.Condition;
import org.nanodb.backend.primitives.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Table {
    public abstract StructType getSchema();
    public abstract int getInitialId();

    // Returns all values that match the select statement
    public List<Map<String, Object>> selectAll() {
        return select(BooleanCondition.TRUE);
    }
    public abstract List<Map<String, Object>> select(Condition condition);
    public abstract List<Map<String, Object>> select(Condition condition, int limit);

    // Returns the count of deleted rows
    public abstract int delete(Condition condition);
    public abstract int delete(Condition condition, int limit);

    public abstract int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction);
    public abstract int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit);

    public abstract void insert(List<Map<String, Object>> values);
    public void insert(Map<String, Object> value) {
         List<Map<String, Object>> values = new ArrayList<>();
         values.add(value);
         this.insert(values);
     }
     public abstract void clear();
}