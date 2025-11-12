package org.nanodb.statement;

import org.nanodb.condition.*;
import org.nanodb.primitives.*;
import org.nanodb.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StatementParser {

    public static PrepareResult prepareStatement(String input, Statement statement) {
        String inputLower = input.trim().toLowerCase();
        input = input.trim();
        if (inputLower.startsWith("create table ")) {
            // Parse: CREATE TABLE table_name (col1 TYPE, col2 TYPE, ...)
            statement.type = StatementType.CREATE_TABLE;
            String rest = input.substring(13).trim();
            
            int openParen = rest.indexOf('(');
            if (openParen == -1) {
                // Simple CREATE TABLE without column definitions
                statement.tableName = rest.trim();
                return PrepareResult.SUCCESS;
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
            
            // Find table name (before opening parenthesis or VALUES)
            int openParen = rest.indexOf('(');
            int valuesIndex = rest.toLowerCase().indexOf("values");
            
            if (openParen == -1 || valuesIndex == -1) {
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
                valuesToInsert.put(col, val);
            }
            
            statement.valuesToInsert = valuesToInsert;
            statement.type = StatementType.INSERT;
            return PrepareResult.SUCCESS;
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
                // Remove quotes if present but keep as string
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
        if (op == ComparisonOperation.GREATER_EQUALS || op == ComparisonOperation.LESS_EQUALS || op == ComparisonOperation.NOT_EQUALS) {
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