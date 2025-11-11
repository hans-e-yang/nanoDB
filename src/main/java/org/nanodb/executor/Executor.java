package org.nanodb.executor;

import org.nanodb.statement.Statement;
import org.nanodb.db.*;
import org.nanodb.primitives.*;
import org.nanodb.utils.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executor {

    public static ExecuteResult execute(Statement statement, Db db) throws IOException {
        switch (statement.type) {
            case CREATE_TABLE:
                if (db.createTable(statement.tableName, statement.tableSchema)) {
                    return ExecuteResult.SUCCESS;
                } else {
                    return ExecuteResult.ERROR;
                }
            case DROP_TABLE:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                if (db.dropTable(statement.tableName)) {
                    return ExecuteResult.SUCCESS;
                } else {
                    return ExecuteResult.ERROR;
                }
            case INSERT:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                // Get schema and parse values according to types
                StructType schema = db.getTableSchema(statement.tableName);
                Map<String, Object> parsedInsertValues;
                try {
                    parsedInsertValues = parseValues(statement.valuesToInsert, schema);
                } catch (Exception e) {
                    return ExecuteResult.DATA_TYPE_MISMATCH;
                }
                if (db.insert(statement.tableName, parsedInsertValues)) {
                    return ExecuteResult.SUCCESS;
                } else {
                    return ExecuteResult.DUPLICATE_KEY;
                }
            case SELECT:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                List<Map<String, Object>> results = db.select(statement.tableName, statement.whereCondition);
                for (Map<String, Object> row : results) {
                    System.out.println(formatRow(row, statement.columns));
                }
                return ExecuteResult.SUCCESS;
            case UPDATE:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                // Get schema and parse values according to types
                StructType updateSchema = db.getTableSchema(statement.tableName);
                Map<String, Object> parsedUpdateValues;
                try {
                    parsedUpdateValues = parseValues(statement.setValues, updateSchema);
                } catch (Exception e) {
                    return ExecuteResult.DATA_TYPE_MISMATCH;
                }
                int updateCount = db.update(statement.tableName, parsedUpdateValues, statement.whereCondition);
                System.out.println("Updated " + updateCount + " row(s).");
                return ExecuteResult.SUCCESS;
            case DELETE:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                int deleteCount = db.delete(statement.tableName, statement.whereCondition);
                System.out.println("Deleted " + deleteCount + " row(s).");
                return ExecuteResult.SUCCESS;
            default:
                return ExecuteResult.ERROR;
        }
    }

    private static String formatRow(Map<String, Object> row, List<String> columns) {
        StringBuilder sb = new StringBuilder("(");
        if (columns == null || columns.isEmpty()) {
            // Print all columns
            boolean first = true;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getValue());
                first = false;
            }
        } else {
            // Print only specified columns
            for (int i = 0; i < columns.size(); i++) {
                String col = columns.get(i);
                if (row.containsKey(col)) {
                    sb.append(row.get(col));
                } else {
                    throw new RuntimeException("Column not found: " + col);
                }
                if (i < columns.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Parse string values according to the schema types
     * @param stringValues Map of column names to string values
     * @param schema The StructType schema defining expected types
     * @return Map of column names to parsed Object values
     * @throws Exception if parsing fails
     */
    private static Map<String, Object> parseValues(Map<String, String> stringValues, StructType schema) throws Exception {
        if (schema == null) {
            throw new Exception("Schema is null");
        }
        
        Map<String, Object> parsedValues = new HashMap<>();
        
        for (Map.Entry<String, String> entry : stringValues.entrySet()) {
            String columnName = entry.getKey();
            String stringValue = entry.getValue();
            
            // Find the column type in schema
            DataType<?> expectedType = null;
            for (Pair<String, DataType<?>> field : schema.getFields()) {
                if (field.getKey().equals(columnName)) {
                    expectedType = field.getValue();
                    break;
                }
            }
            
            if (expectedType == null) {
                throw new Exception("Column not found in schema: " + columnName);
            }
            
            // Parse the string value to the appropriate type
            Object parsedValue = parseValueByType(stringValue, expectedType);
            parsedValues.put(columnName, parsedValue);
        }
        
        return parsedValues;
    }

    /**
     * Parse a string value to the appropriate type based on DataType
     */
    private static Object parseValueByType(String value, DataType<?> dataType) throws Exception {
        if (value == null) {
            return null;
        }
        
        value = value.trim();
        
        if (dataType instanceof Int32Type) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new Exception("Cannot parse '" + value + "' as integer");
            }
        } else if (dataType instanceof VarCharType) {
            return value; // Already a string
        } else if (dataType instanceof DateType) {
            // Try multiple date formats
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyy/MM/dd"),
                new SimpleDateFormat("dd-MM-yyyy"),
                new SimpleDateFormat("dd/MM/yyyy")
            };
            
            for (SimpleDateFormat format : dateFormats) {
                try {
                    return format.parse(value);
                } catch (ParseException e) {
                    // Try next format
                }
            }
            throw new Exception("Cannot parse '" + value + "' as date");
        } else {
            throw new Exception("Unsupported data type: " + dataType.getClass().getName());
        }
    }
}