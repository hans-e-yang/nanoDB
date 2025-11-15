package org.nanodb.utils;

import org.nanodb.internal.condition.BooleanCondition;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.primitives.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Table<T> {

    // Returns all values that match the select statement
    public List<T> selectAll() { return select(BooleanCondition.TRUE); }
    public List<T> select(Condition condition) { return select(condition, Integer.MAX_VALUE); }
    public abstract List<T> select(Condition condition, int limit);

    // Returns the count of deleted rows
    public int delete(Condition condition) { return delete(condition, Integer.MAX_VALUE);}
    public abstract int delete(Condition condition, int limit);

    public int update(Condition condition, Function<T, T> updateFunction) { return update(condition, updateFunction, Integer.MAX_VALUE); }
    public abstract int update(Condition condition, Function<T, T> updateFunction, int limit);

    public abstract void insert(List<T> values);
    public void insert(T value) {
         List<T> values = new ArrayList<>();
         values.add(value);
         this.insert(values);
     }
     public abstract void clear();
}