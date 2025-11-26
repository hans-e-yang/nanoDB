package org.nanodb.cli.executor;

import org.nanodb.cli.statement.Statement;
import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.db.*;
import org.nanodb.internal.primitives.*;
import org.nanodb.cli.parser.ValueParser;
import org.nanodb.internal.condition.BooleanCondition;
import org.nanodb.internal.condition.Condition;

import java.io.IOException;
import java.text.ParseException;
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
                    parsedInsertValues = ValueParser.parseValues(statement.valuesToInsert, schema);
                } catch (ParseException e) {
                    return ExecuteResult.DATA_TYPE_MISMATCH;
                }
                // We don't throw any errors in insert for now..
                db.getTable(statement.tableName).insert(parsedInsertValues);
                return ExecuteResult.SUCCESS;
//                if (db.getTable(statement.tableName).insert(parsedInsertValues)) {
//                    return ExecuteResult.SUCCESS;
//                } else {
//                    return ExecuteResult.DUPLICATE_KEY;
//                }
            case SELECT:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                // Parse WHERE condition values according to schema
                Condition parsedSelectCondition = statement.whereCondition;
                if (statement.whereCondition != null) {
                    StructType selectSchema = db.getTableSchema(statement.tableName);
                    try {
                        parsedSelectCondition = ValueParser.parseConditionValues(statement.whereCondition, selectSchema);
                    } catch (ParseException e) {
                        return ExecuteResult.DATA_TYPE_MISMATCH;
                    }
                } else {
                    parsedSelectCondition = BooleanCondition.TRUE;
                }
                List<Map<String, Object>> results;
                try {
                    results = db.getTable(statement.tableName).select(parsedSelectCondition, statement.limit);
                } catch (MissingWhereConditionException e) {
                    System.out.println(e.getMessage());
                    return ExecuteResult.ERROR;
                }
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
                Condition parsedUpdateCondition = statement.whereCondition;
                try {
                    parsedUpdateValues = ValueParser.parseValues(statement.setValues, updateSchema);
                    // Parse WHERE condition values according to schema
                    if (statement.whereCondition != null) {
                        parsedUpdateCondition = ValueParser.parseConditionValues(parsedUpdateCondition, updateSchema);
                    } else {
                        parsedUpdateCondition = BooleanCondition.TRUE;
                    }
                } catch (ParseException e) {
                    return ExecuteResult.DATA_TYPE_MISMATCH;
                }
                // Create update function that merges the parsed update values into the row
                int updateCount;
                try {
                    updateCount = db.getTable(statement.tableName).update(
                        parsedUpdateCondition,
                        row -> {
                            Map<String, Object> updatedRow = new java.util.HashMap<>(row);
                            updatedRow.putAll(parsedUpdateValues);
                            return updatedRow;
                        },
                        statement.limit
                    );
                } catch (MissingWhereConditionException e) {
                    System.out.println(e.getMessage());
                    return ExecuteResult.ERROR;
                }
                System.out.println("Updated " + updateCount + " row(s).");
                return ExecuteResult.SUCCESS;
            case DELETE:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                // Parse WHERE condition values according to schema
                Condition parsedDeleteCondition = statement.whereCondition;
                if (statement.whereCondition != null) {
                    StructType deleteSchema = db.getTableSchema(statement.tableName);
                    try {
                        parsedDeleteCondition = ValueParser.parseConditionValues(statement.whereCondition, deleteSchema);
                    } catch (ParseException e) {
                        return ExecuteResult.DATA_TYPE_MISMATCH;
                    }
                } else {
                    parsedDeleteCondition = BooleanCondition.TRUE;
                }
                int deleteCount;
                try {
                    deleteCount = db.getTable(statement.tableName).delete(parsedDeleteCondition, statement.limit);
                } catch (MissingWhereConditionException e) {
                    System.out.println(e.getMessage());
                    return ExecuteResult.ERROR;
                }
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
}