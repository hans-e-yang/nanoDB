package org.nanodb.executor;

import org.nanodb.statement.Statement;
import org.nanodb.db.*;

import java.io.IOException;
import java.util.List;

public class Executor {

    public static ExecuteResult execute(Statement statement, Db db) throws IOException {
        switch (statement.type) {
            case CREATE_TABLE:
                if (db.createTable(statement.tableName)) {
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
                if (db.tableExists(statement.tableName)) {
                    if (db.insert(statement.tableName, statement.rowToInsert)) {
                        return ExecuteResult.SUCCESS;
                    } else {
                        return ExecuteResult.DUPLICATE_KEY;
                    }
                } else {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
            case SELECT:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                List<Row> results = db.select(statement.tableName, statement.whereCondition);
                for (Row row : results) {
                    System.out.println(formatRow(row, statement.columns));
                }
                return ExecuteResult.SUCCESS;
            case UPDATE:
                if (!db.tableExists(statement.tableName)) {
                    return ExecuteResult.TABLE_NOT_FOUND;
                }
                int updateCount = db.update(statement.tableName, statement.setValues, statement.whereCondition);
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

    private static String formatRow(Row row, List<String> columns) {
        StringBuilder sb = new StringBuilder("(");
        if (columns == null) {
            sb.append(row.id).append(", ").append(row.courseName).append(", ").append(row.courseCode);
        } else if (columns.isEmpty()) {
            sb.append(row.id).append(", ").append(row.courseName).append(", ").append(row.courseCode);
        } else {
            for (int i = 0; i < columns.size(); i++) {
                String col = columns.get(i);
                if (col.equals("id")) {
                    sb.append(row.id);
                } else if (col.equals("courseName")) {
                    sb.append(row.courseName);
                } else if (col.equals("courseCode")) {
                    sb.append(row.courseCode);
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