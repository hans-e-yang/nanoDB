package org.nanodb.frontend.cli.statement;

import org.nanodb.backend.condition.Condition;
import org.nanodb.backend.primitives.StructType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statement {
    public StatementType type;
    public String tableName;
    public Condition whereCondition;
    public Map<String, String> setValues = new HashMap<>();
    public List<String> columns;
    public Map<String, String> valuesToInsert = new HashMap<>();
    public StructType tableSchema;
}