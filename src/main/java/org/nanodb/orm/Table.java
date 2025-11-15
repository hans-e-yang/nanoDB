package org.nanodb.orm;

import org.nanodb.internal.condition.BooleanCondition;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.orm.internal.ClassMapSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Table<T extends BaseModel> extends org.nanodb.utils.Table<T> {
    InternalTable table;
    ClassMapSerializer<T> serializer;
    public Table(InternalTable table, Class<?> type) {
        this.table = table;
        this.serializer = new ClassMapSerializer<T>((Class<T>)type);
    }

    public List<T> select(Condition condition, int limit) {
        return table.select(condition, limit).stream().map(serializer::serialize).toList();
    }

    public int delete(Condition condition, int limit) { return table.delete(condition, limit); }

    public int update(Condition condition, Function<T, T> updateFunction, int limit) {
        Function<Map<String, Object>, Map<String, Object>> wrappedUpdateFunction = src ->
                serializer.deserialize(updateFunction.apply(serializer.serialize(src)));

        return table.update(condition, wrappedUpdateFunction, limit);
    }

    public void insert(List<T> values) {
        table.insert(values.stream().map(serializer::deserialize).toList());
    }
    public void clear() {table.clear();}
}
