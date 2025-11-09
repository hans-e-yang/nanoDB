package org.nanodb.table;

import org.nanodb.condition.Condition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Table {
    // Returns all values that match the select statement
    public List<Map<String, Object>> select(Condition condition);
    public List<Map<String, Object>> select(Condition condition, int limit);

    // Returns the count of deleted rows
    public int delete(Condition condition);
    public int delete(Condition condition, int limit);

    public void update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction);
    public void update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit);

    public void insert(List<Map<String, Object>> values);
    public void clear();
}
