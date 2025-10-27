package org.nanodb.statement;

import org.nanodb.db.Row;
import org.nanodb.statement.condition.*;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {

    public static PrepareResult prepareStatement(String input, Statement statement) {
        String inputLower = input.trim().toLowerCase();
        input = input.trim();
        if (inputLower.startsWith("create table ")) {
            statement.type = StatementType.CREATE_TABLE;
            statement.tableName = input.substring(13).trim();
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("drop table ")) {
            statement.type = StatementType.DROP_TABLE;
            statement.tableName = input.substring(11).trim();
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("insert into ")) {
            String rest = input.substring(12).trim();
            String[] parts = rest.split(" ");
            if (parts.length < 4) return PrepareResult.SYNTAX_ERROR;
            statement.tableName = parts[0];
            try {
                int id = Integer.parseInt(parts[1]);
                if (id < 0) return PrepareResult.NEGATIVE_ID;
                String courseName = parts[2];
                if (courseName.length() > Row.COURSE_NAME_SIZE) return PrepareResult.STRING_TOO_LONG;
                String courseCode = parts[3];
                if (courseCode.length() > Row.COURSE_CODE_SIZE) return PrepareResult.STRING_TOO_LONG;
                statement.rowToInsert = new Row(id, courseName, courseCode);
                statement.type = StatementType.INSERT;
                return PrepareResult.SUCCESS;
            } catch (NumberFormatException e) {
                return PrepareResult.SYNTAX_ERROR;
            }
        } else if (inputLower.startsWith("select ")) {
            int fromIndex = inputLower.indexOf(" from ");
            if (fromIndex == -1) return PrepareResult.SYNTAX_ERROR;
            String columnsStr = input.substring(7, fromIndex).trim();
            String rest = input.substring(fromIndex + 6).trim();
            String[] whereParts = rest.split("(?i) where ");
            statement.tableName = whereParts[0].trim();
            statement.type = StatementType.SELECT;
            if (columnsStr.equals("*")) {
                statement.columns = null; // null means all
            } else {
                String[] cols = columnsStr.split(",");
                List<String> colList = new ArrayList<>();
                for (String col : cols) {
                    colList.add(col.trim());
                }
                statement.columns = colList;
            }
            if (whereParts.length > 1) {
                try {
                    statement.whereCondition = parseWhere(whereParts[1]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
            }
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("update ")) {
            String[] whereParts = input.split("(?i) where ");
            String updatePart = whereParts[0];
            int setIndex = updatePart.toLowerCase().indexOf(" set ");
            if (setIndex == -1) return PrepareResult.SYNTAX_ERROR;
            statement.tableName = updatePart.substring(7, setIndex).trim();
            String setStr = updatePart.substring(setIndex + 5).trim();
            String[] setPairs = setStr.split(",");
            for (String pair : setPairs) {
                String[] kv = pair.trim().split("=");
                if (kv.length != 2) return PrepareResult.SYNTAX_ERROR;
                String col = kv[0].trim();
                String val = kv[1].trim();
                if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }
                statement.setValues.put(col, val);
            }
            statement.type = StatementType.UPDATE;
            if (whereParts.length > 1) {
                try {
                    statement.whereCondition = parseWhere(whereParts[1]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
            }
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("delete from ")) {
            String[] whereParts = input.split("(?i) where ");
            String deletePart = whereParts[0];
            statement.tableName = deletePart.substring(12).trim();
            statement.type = StatementType.DELETE;
            if (whereParts.length > 1) {
                try {
                    statement.whereCondition = parseWhere(whereParts[1]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
            }
            return PrepareResult.SUCCESS;
        }
        return PrepareResult.UNRECOGNIZED_STATEMENT;
    }

    private static Condition parseWhere(String whereStr) {
        String[] orParts = whereStr.split(" or ");
        Condition cond = parseAnd(orParts[0]);
        for (int i = 1; i < orParts.length; i++) {
            cond = new OrCondition(cond, parseAnd(orParts[i]));
        }
        return cond;
    }

    private static Condition parseAnd(String str) {
        String[] andParts = str.split(" and ");
        Condition cond = parseComp(andParts[0]);
        for (int i = 1; i < andParts.length; i++) {
            cond = new AndCondition(cond, parseComp(andParts[i]));
        }
        return cond;
    }

    private static Condition parseComp(String str) {
        str = str.trim();
        String op = null;
        int opIndex = -1;
        if ((opIndex = str.indexOf('=')) > 0) {
            op = "=";
        } else if ((opIndex = str.indexOf('>')) > 0) {
            op = ">";
        } else if ((opIndex = str.indexOf('<')) > 0) {
            op = "<";
        }
        if (opIndex == -1) {
            throw new RuntimeException("Invalid condition");
        }
        String col = str.substring(0, opIndex).trim();
        String val = str.substring(opIndex + 1).trim();
        if (val.startsWith("'") && val.endsWith("'")) {
            val = val.substring(1, val.length() - 1);
        }
        return new ComparisonCondition(col, op, val);
    }
}