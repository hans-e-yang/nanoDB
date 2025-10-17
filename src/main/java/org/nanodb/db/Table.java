package org.nanodb.db;

import org.nanodb.statement.condition.Condition;

import java.util.*;

/**
 * Represents a single table in the database
 */
public class Table {
    private String tableName;
    private List<Row> rows = new ArrayList<>();
    private Set<Integer> usedIds = new HashSet<>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Inserts a new row into the table
     * @param row The row to insert
     * @return true if insert was successful, false if duplicate key
     */
    public boolean insert(Row row) {
        if (usedIds.contains(row.id)) {
            return false; // Duplicate key
        }
        rows.add(row);
        usedIds.add(row.id);
        return true;
    }

    /**
     * Selects rows matching the given condition
     * @param condition The condition to filter by (null means all rows)
     * @return List of matching rows
     */
    public List<Row> select(Condition condition) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (condition == null || condition.evaluate(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Deletes rows matching the given condition
     * @param condition The condition to filter by (null means all rows)
     * @return Number of rows deleted
     */
    public int delete(Condition condition) {
        int count = 0;
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (condition == null || condition.evaluate(row)) {
                usedIds.remove(row.id);
                iterator.remove();
                count++;
            }
        }
        return count;
    }

    /**
     * Updates rows matching the given condition
     * @param setValues Map of column name to new value
     * @param condition The condition to filter by (null means all rows)
     * @return Number of rows updated
     */
    public int update(Map<String, String> setValues, Condition condition) {
        int count = 0;
        for (Row row : rows) {
            if (condition == null || condition.evaluate(row)) {
                updateRow(row, setValues);
                count++;
            }
        }
        return count;
    }

    private void updateRow(Row row, Map<String, String> setValues) {
        for (Map.Entry<String, String> entry : setValues.entrySet()) {
            String col = entry.getKey();
            String val = entry.getValue();
            if (col.equals("id")) {
                row.id = Integer.parseInt(val);
            } else if (col.equals("courseName")) {
                row.courseName = val;
            } else if (col.equals("courseCode")) {
                row.courseCode = val;
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public int getRowCount() {
        return rows.size();
    }

    public List<Row> getAllRows() {
        return new ArrayList<>(rows);
    }
}
