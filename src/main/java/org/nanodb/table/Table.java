package org.nanodb.table;

import org.nanodb.condition.Condition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Table {
    // Returns all values that match the select statement
     List<Map<String, Object>> select(Condition condition);
     List<Map<String, Object>> select(Condition condition, int limit);

    // Returns the count of deleted rows
     int delete(Condition condition);
     int delete(Condition condition, int limit);

     int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction);
     int update(Condition condition, Function<Map<String, Object>, Map<String, Object>> updateFunction, int limit);

     void insert(List<Map<String, Object>> values);
     void clear();
}
