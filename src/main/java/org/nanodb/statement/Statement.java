package org.nanodb.statement;

import org.nanodb.db.Row;
import org.nanodb.statement.condition.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statement {
    public StatementType type;
    public Row rowToInsert;
    public String tableName;
    public Condition whereCondition;
    public Map<String, String> setValues = new HashMap<>();
    public List<String> columns;
}