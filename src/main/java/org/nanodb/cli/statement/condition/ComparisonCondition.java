package org.nanodb.cli.statement.condition;

import org.nanodb.internal.db.Row;

public class ComparisonCondition extends Condition {
    private String column;
    private String op;
    private String value;

    public ComparisonCondition(String column, String op, String value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    @Override
    public boolean evaluate(Row row) {
        if (column.equalsIgnoreCase("id")) {
            int rowVal = row.id;
            int valInt;
            try {
                valInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return false;
            }
            switch (op) {
                case "=":
                    return rowVal == valInt;
                case ">":
                    return rowVal > valInt;
                case "<":
                    return rowVal < valInt;
                default:
                    return false;
            }
        } else if (column.equalsIgnoreCase("courseName")) {
            if (!op.equals("=")) return false;
            return row.courseName.equals(value);
        } else if (column.equalsIgnoreCase("courseCode")) {
            if (!op.equals("=")) return false;
            return row.courseCode.equals(value);
        }
        return false;
    }
}