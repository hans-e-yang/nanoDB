package org.nanodb.cli.statement;

import org.nanodb.internal.condition.*;
import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StatementParser {

    /**
     * Parse a SQL statement string and return a Statement object
     * 
     * @param input The SQL statement string to parse
     * @return A Statement object representing the parsed statement
     * @throws RuntimeException if the statement cannot be parsed
     */
    public static Statement parse(String input) {
        Statement statement = new Statement();
        PrepareResult result = prepareStatement(input, statement);

        if (result != PrepareResult.SUCCESS) {
            throw new RuntimeException("Failed to parse statement: " + input + " (Result: " + result + ")");
        }

        return statement;
    }

    public static PrepareResult prepareStatement(String input, Statement statement) {
        String inputLower = input.trim().toLowerCase();
        input = input.trim();
        if (inputLower.startsWith("create table ")) {
            // Parse: CREATE TABLE table_name (col1 TYPE, col2 TYPE, ...)
            statement.type = StatementType.CREATE_TABLE;
            String rest = input.substring(13).trim();

            int openParen = rest.indexOf('(');
            if (openParen == -1) {
                // CREATE TABLE without column definitions is a syntax error
                return PrepareResult.SYNTAX_ERROR;
            }

            statement.tableName = rest.substring(0, openParen).trim();

            int closeParen = rest.lastIndexOf(')');
            if (closeParen == -1) {
                return PrepareResult.SYNTAX_ERROR;
            }

            String columnsStr = rest.substring(openParen + 1, closeParen).trim();
            String[] columnDefs = columnsStr.split(",");

            List<Pair<String, DataType<?>>> fields = new ArrayList<>();

            for (String columnDef : columnDefs) {
                String[] parts = columnDef.trim().split("\\s+");
                if (parts.length != 2) {
                    return PrepareResult.SYNTAX_ERROR;
                }
                String columnName = parts[0].trim();
                String columnType = parts[1].trim().toLowerCase();

                // Map type string to DataType instance
                DataType<?> dataType;

                if (columnType.equals("int")) {
                    dataType = new Int32Type();
                } else if (columnType.startsWith("varchar(") && columnType.endsWith(")")) {
                    // Parse varchar(length)
                    String lengthStr = columnType.substring(8, columnType.length() - 1).trim();
                    try {
                        int length = Integer.parseInt(lengthStr);
                        if (length <= 0) {
                            return PrepareResult.SYNTAX_ERROR;
                        }
                        dataType = new VarCharType(length);
                    } catch (NumberFormatException e) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                } else if (columnType.equals("date")) {
                    dataType = new DateType();
                } else {
                    return PrepareResult.SYNTAX_ERROR;
                }

                fields.add(new Pair<>(columnName, dataType));
            }

            statement.tableSchema = new StructType(fields);

            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("drop table ")) {
            statement.type = StatementType.DROP_TABLE;
            statement.tableName = input.substring(11).trim();
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("insert into ")) {
            // Parse: INSERT INTO table_name (col1, col2, ...) VALUES (val1, val2, ...)
            String rest = input.substring(12).trim();

            // Find table name (before opening parenthesis)
            int openParen = rest.indexOf('(');
            if (openParen == -1) {
                return PrepareResult.SYNTAX_ERROR;
            }
            statement.tableName = rest.substring(0, openParen).trim();

            // Extract column names
            int closeParen = rest.indexOf(')', openParen);
            if (closeParen == -1) {
                return PrepareResult.SYNTAX_ERROR;
            }
            String columnsStr = rest.substring(openParen + 1, closeParen).trim();
            String[] columns = columnsStr.split(",");

            // Extract values
            int valuesIndex = rest.toLowerCase().indexOf("values");
            if (valuesIndex == -1) {
                return PrepareResult.SYNTAX_ERROR;
            }
            String valuesStr = rest.substring(valuesIndex + 6).trim(); // Skip "values"
            int valuesOpen = valuesStr.indexOf('(');
            int valuesClose = valuesStr.indexOf(')');
            if (valuesOpen == -1 || valuesClose == -1) {
                return PrepareResult.SYNTAX_ERROR;
            }
            String valuesContent = valuesStr.substring(valuesOpen + 1, valuesClose).trim();
            String[] values = valuesContent.split(",");

            if (columns.length != values.length) {
                return PrepareResult.SYNTAX_ERROR;
            }

            // Build the valuesToInsert map (keep as strings)
            Map<String, String> valuesToInsert = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i].trim();
                String val = values[i].trim();
                // Remove quotes if present but keep as string
                if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }
                // Handle null keyword (case-insensitive)
                if (val.equalsIgnoreCase("null")) {
                    valuesToInsert.put(col, null);
                } else {
                    valuesToInsert.put(col, val);
                }
            }
            statement.valuesToInsert = valuesToInsert;

            statement.type = StatementType.INSERT;
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("select ")) {
            int fromIndex = inputLower.indexOf(" from ");
            if (fromIndex == -1)
                return PrepareResult.SYNTAX_ERROR;
            String columnsStr = input.substring(7, fromIndex).trim();
            String rest = input.substring(fromIndex + 6).trim();
            String[] whereParts = rest.split("(?i) where ");

            // Check for LIMIT clause
            String tableAndLimit = whereParts[0].trim();
            String[] limitParts = tableAndLimit.split("(?i) limit ");
            statement.tableName = limitParts[0].trim();

            if (limitParts.length > 1) {
                try {
                    statement.limit = Integer.parseInt(limitParts[1].trim());
                    if (statement.limit < 0) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                } catch (NumberFormatException e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
            }

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
                // Parse WHERE clause - may contain LIMIT
                String whereClause = whereParts[1];
                String[] whereLimitParts = whereClause.split("(?i) limit ");
                try {
                    statement.whereCondition = parseWhere(whereLimitParts[0]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
                if (whereLimitParts.length > 1) {
                    try {
                        statement.limit = Integer.parseInt(whereLimitParts[1].trim());
                        if (statement.limit < 0) {
                            return PrepareResult.SYNTAX_ERROR;
                        }
                    } catch (NumberFormatException e) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                }
            } else {
                statement.whereCondition = BooleanCondition.TRUE;
            }
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("update ")) {
            String[] whereParts = input.split("(?i) where ");
            String updatePart = whereParts[0];
            int setIndex = updatePart.toLowerCase().indexOf(" set ");
            if (setIndex == -1)
                return PrepareResult.SYNTAX_ERROR;
            statement.tableName = updatePart.substring(7, setIndex).trim();
            String setStr = updatePart.substring(setIndex + 5).trim();
            String[] setPairs = setStr.split(",");
            for (String pair : setPairs) {
                String[] kv = pair.trim().split("=");
                if (kv.length != 2)
                    return PrepareResult.SYNTAX_ERROR;
                String col = kv[0].trim();
                String val = kv[1].trim();
                // Remove quotes if present but keep as string
                if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }
                // Handle null keyword (case-insensitive)
                if (val.equalsIgnoreCase("null")) {
                    statement.setValues.put(col, null);
                } else {
                    statement.setValues.put(col, val);
                }
            }
            statement.type = StatementType.UPDATE;
            if (whereParts.length > 1) {
                // Parse WHERE clause - may contain LIMIT
                String whereClause = whereParts[1];
                String[] whereLimitParts = whereClause.split("(?i) limit ");
                try {
                    statement.whereCondition = parseWhere(whereLimitParts[0]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
                if (whereLimitParts.length > 1) {
                    try {
                        statement.limit = Integer.parseInt(whereLimitParts[1].trim());
                        if (statement.limit < 0) {
                            return PrepareResult.SYNTAX_ERROR;
                        }
                    } catch (NumberFormatException e) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                }
            } else {
                statement.whereCondition = BooleanCondition.TRUE;
            }
            return PrepareResult.SUCCESS;
        } else if (inputLower.startsWith("delete from ")) {
            String[] whereParts = input.split("(?i) where ");
            String deletePart = whereParts[0];

            // Check for LIMIT clause in DELETE without WHERE
            String tableAndLimit = deletePart.substring(12).trim();
            String[] limitParts = tableAndLimit.split("(?i) limit ");
            statement.tableName = limitParts[0].trim();

            if (limitParts.length > 1) {
                try {
                    statement.limit = Integer.parseInt(limitParts[1].trim());
                    if (statement.limit < 0) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                } catch (NumberFormatException e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
            }

            statement.type = StatementType.DELETE;
            if (whereParts.length > 1) {
                // Parse WHERE clause - may contain LIMIT
                String whereClause = whereParts[1];
                String[] whereLimitParts = whereClause.split("(?i) limit ");
                try {
                    statement.whereCondition = parseWhere(whereLimitParts[0]);
                } catch (Exception e) {
                    return PrepareResult.SYNTAX_ERROR;
                }
                if (whereLimitParts.length > 1) {
                    try {
                        statement.limit = Integer.parseInt(whereLimitParts[1].trim());
                        if (statement.limit < 0) {
                            return PrepareResult.SYNTAX_ERROR;
                        }
                    } catch (NumberFormatException e) {
                        return PrepareResult.SYNTAX_ERROR;
                    }
                }
            } else {
                statement.whereCondition = BooleanCondition.TRUE;
            }
            return PrepareResult.SUCCESS;
        }
        return PrepareResult.UNRECOGNIZED_STATEMENT;
    }

    private static Condition parseWhere(String whereStr) {
        whereStr = whereStr.trim();
        return parseOrExpression(whereStr);
    }

    /**
     * Parse OR expression with proper parentheses handling.
     * Splits on " OR " only when not inside parentheses.
     */
    private static Condition parseOrExpression(String str) {
        str = str.trim();
        
        // Find " or " at the top level (not inside parentheses)
        int depth = 0;
        int lastSplit = 0;
        java.util.List<String> parts = new java.util.ArrayList<>();
        String lowerStr = str.toLowerCase();
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && i + 4 <= str.length() && lowerStr.substring(i, i + 4).equals(" or ")) {
                parts.add(str.substring(lastSplit, i));
                lastSplit = i + 4;
            }
        }
        parts.add(str.substring(lastSplit));
        
        if (parts.size() == 1) {
            return parseAndExpression(parts.get(0));
        }
        
        Condition cond = parseAndExpression(parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            cond = new OrCondition(cond, parseAndExpression(parts.get(i)));
        }
        return cond;
    }

    /**
     * Parse AND expression with proper parentheses handling.
     * Splits on " AND " only when not inside parentheses.
     */
    private static Condition parseAndExpression(String str) {
        str = str.trim();
        
        // Find " and " at the top level (not inside parentheses)
        int depth = 0;
        int lastSplit = 0;
        java.util.List<String> parts = new java.util.ArrayList<>();
        String lowerStr = str.toLowerCase();
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && i + 5 <= str.length() && lowerStr.substring(i, i + 5).equals(" and ")) {
                parts.add(str.substring(lastSplit, i));
                lastSplit = i + 5;
            }
        }
        parts.add(str.substring(lastSplit));
        
        if (parts.size() == 1) {
            return parsePrimaryCondition(parts.get(0));
        }
        
        Condition cond = parsePrimaryCondition(parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            cond = new AndCondition(cond, parsePrimaryCondition(parts.get(i)));
        }
        return cond;
    }

    /**
     * Parse a primary condition which is either a parenthesized expression or a comparison.
     */
    private static Condition parsePrimaryCondition(String str) {
        str = str.trim();
        
        // Check if the entire expression is wrapped in parentheses
        if (str.startsWith("(") && str.endsWith(")")) {
            // Verify that the parentheses are balanced and outer
            int depth = 0;
            boolean outerParens = true;
            for (int i = 0; i < str.length() - 1; i++) {
                if (str.charAt(i) == '(') {
                    depth++;
                } else if (str.charAt(i) == ')') {
                    depth--;
                    if (depth == 0) {
                        outerParens = false;
                        break;
                    }
                }
            }
            if (outerParens) {
                // Remove outer parentheses and parse inner expression
                return parseOrExpression(str.substring(1, str.length() - 1));
            }
        }
        
        return parseComp(str);
    }

    private static Condition parseComp(String str) {
        str = str.trim();
        ComparisonOperation op = null;
        int opIndex = -1;

        // Check for two-character operators first
        if ((opIndex = str.indexOf(">=")) > 0) {
            op = ComparisonOperation.GREATER_EQUALS;
            opIndex = str.indexOf(">=");
        } else if ((opIndex = str.indexOf("<=")) > 0) {
            op = ComparisonOperation.LESS_EQUALS;
            opIndex = str.indexOf("<=");
        } else if ((opIndex = str.indexOf("!=")) > 0) {
            op = ComparisonOperation.NOT_EQUALS;
            opIndex = str.indexOf("!=");
        } else if (str.toLowerCase().contains(" like ")) {
            op = ComparisonOperation.LIKE;
            opIndex = str.toLowerCase().indexOf(" like ");
        } else if ((opIndex = str.indexOf('=')) > 0) {
            op = ComparisonOperation.EQUAL;
        } else if ((opIndex = str.indexOf('>')) > 0) {
            op = ComparisonOperation.GREATER_THAN;
        } else if ((opIndex = str.indexOf('<')) > 0) {
            op = ComparisonOperation.LESS_THAN;
        }

        if (opIndex == -1 || op == null) {
            throw new RuntimeException("Invalid condition");
        }

        String col = str.substring(0, opIndex).trim();
        int valStart = opIndex;
        if (op == ComparisonOperation.GREATER_EQUALS || op == ComparisonOperation.LESS_EQUALS
                || op == ComparisonOperation.NOT_EQUALS) {
            valStart += 2;
        } else if (op == ComparisonOperation.LIKE) {
            valStart += 6; // " like ".length()
        } else {
            valStart += 1;
        }

        String val = str.substring(valStart).trim();
        if (val.startsWith("'") && val.endsWith("'")) {
            val = val.substring(1, val.length() - 1);
        }
        return new ComparisonCondition(col, op, val);
    }
}