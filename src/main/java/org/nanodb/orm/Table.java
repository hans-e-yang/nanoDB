package org.nanodb.orm;

import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.condition.Condition;
import org.nanodb.internal.primitives.*;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.orm.internal.ClassMapSerializer;
import org.nanodb.orm.internal.ClassToDataType;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Table<T extends BaseModel> extends org.nanodb.utils.Table<T> {
    InternalTable table;
    ClassMapSerializer<T> serializer;
    StructType schema;
    
    public Table(InternalTable table, Class<?> type) {
        this.table = table;
        this.serializer = new ClassMapSerializer<T>((Class<T>)type);
        this.schema = ClassToDataType.getDataType((Class<? extends BaseModel>)type);
    }

    public List<T> select(Condition condition, int limit) throws MissingWhereConditionException {
        return table.select(condition, limit).stream().map(serializer::serialize).toList();
    }

    public int delete(Condition condition, int limit) throws MissingWhereConditionException { return table.delete(condition, limit); }

    public int update(Condition condition, Function<T, T> updateFunction, int limit) throws MissingWhereConditionException {
        Function<Map<String, Object>, Map<String, Object>> wrappedUpdateFunction = src ->
                convertNullsToDefaults(serializer.deserialize(updateFunction.apply(serializer.serialize(src))));

        return table.update(condition, wrappedUpdateFunction, limit);
    }

    public void insert(List<T> values) {
        table.insert(values.stream()
                .map(serializer::deserialize)
                .map(this::convertNullsToDefaults)
                .toList());
    }
    
    /**
     * Converts null values in a map to default values based on the schema.
     * This is needed because the internal storage system does not support null values.
     * 
     * @param data The map potentially containing null values
     * @return The map with null values replaced by type-appropriate defaults
     */
    private Map<String, Object> convertNullsToDefaults(Map<String, Object> data) {
        for (var field : schema.getFields()) {
            String columnName = field.getKey();
            DataType<?> dataType = field.getValue();
            if (data.get(columnName) == null) {
                data.put(columnName, getDefaultValue(dataType));
            }
        }
        return data;
    }
    
    /**
     * Get the default value for a given data type when null is provided.
     * 
     * @param dataType The data type to get a default value for
     * @return The default value appropriate for the type
     */
    private static Object getDefaultValue(DataType<?> dataType) {
        if (dataType instanceof VarCharType) {
            return "";  // Empty string for VarChar
        } else if (dataType instanceof Int32Type) {
            return 0;   // Zero for integers
        } else if (dataType instanceof DateType) {
            return new Date(0);  // Unix epoch (1970-01-01) for dates
        } else if (dataType instanceof BooleanType) {
            return false;  // False for booleans
        } else if (dataType instanceof BytesType) {
            return new byte[0];  // Empty byte array for bytes
        } else if (dataType instanceof ArrayType) {
            return List.of();  // Empty list for arrays
        } else if (dataType instanceof StructType) {
            // For nested structs, create a map with default values
            StructType structType = (StructType) dataType;
            Map<String, Object> nestedDefaults = new java.util.HashMap<>();
            for (var field : structType.getFields()) {
                nestedDefaults.put(field.getKey(), getDefaultValue(field.getValue()));
            }
            return nestedDefaults;
        } else {
            // For any other type, return empty string as fallback
            return "";
        }
    }
    
    public void clear() {table.clear();}
}
