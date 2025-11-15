package org.nanodb.backend.db;

import org.nanodb.backend.condition.ComparisonCondition;
import org.nanodb.backend.condition.ComparisonOperation;
import org.nanodb.backend.primitives.*;
import org.nanodb.backend.primitives.serializer.DataTypeSerializer;
import org.nanodb.backend.table.Table;
import org.nanodb.backend.tableFactory.TableFactory;
import org.nanodb.utils.Pair;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database extends Db {
    private final Map<String, Table> tables = new HashMap<>();
    private final Table metadata;

    public static final int MAXIMUM_TABLE_NAME_LENGTH = 64;
    public static final int MAXIMUM_TABLE_DEFINITION_LENGTH = 1024;

    private static final String NAME_FIELD = "n";
    private static final String PTR_FIELD = "p";
    private static final String DEFINITION_FIELD = "d";

    private static final StructType tableDefinitionType;
    static {
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>(NAME_FIELD, new VarCharType(MAXIMUM_TABLE_NAME_LENGTH)));
        fields.add(new Pair<>(PTR_FIELD, new Int32Type()));
        fields.add(new Pair<>(DEFINITION_FIELD, new BytesType(MAXIMUM_TABLE_DEFINITION_LENGTH)));
        tableDefinitionType = new StructType(fields);
    }

    public Database(TableFactory factory) {
        super(factory);
        this.metadata = factory.getInitial(tableDefinitionType);
        List<Map<String, Object>> tables = this.metadata.selectAll();
        // Load the table definitions
        for (Map<String, Object> table : tables) {
            String tableName = (String) table.get(NAME_FIELD);
            Integer tableBlockId = (Integer) table.get(PTR_FIELD);
            StructType tableSchema = (StructType) table.get(DEFINITION_FIELD);
            if (this.tables.containsKey(tableName)) {
                throw new TableFileCorruptedException("Duplicate table names exist!");
            }

            this.tables.put(tableName, this.tableFactory.get(tableBlockId, tableSchema));
        }

    }

    @Override
    public boolean createTable(String tableName, StructType schema) {
        if (tables.containsKey(tableName)) return false;
        Table newTable = this.tableFactory.createNew(schema);

        // Write to metadata
        Map<String, Object> tableDefinition = new HashMap<>();
        tableDefinition.put(NAME_FIELD, tableName);
        tableDefinition.put(PTR_FIELD, newTable.getInitialId());
        tableDefinition.put(DEFINITION_FIELD, DataTypeSerializer. .getSchema());
        metadata.insert(tableDefinition);

        // Also add to memory
        tables.put(tableName, newTable);
        return true;
    }

    @Override
    public boolean dropTable(String tableName) {
        if (tables.containsKey(tableName)) return false;
        // clear table
        Table table = this.tables.get(tableName);
        table.clear();

        // remove from metadata where name equal tablename
        metadata.delete(new ComparisonCondition(NAME_FIELD, ComparisonOperation.EQUAL, tableName));

        // remove from memory too
        this.tables.remove(tableName);

        return true;
    }

    @Override
    public Table getTable(String tableName) {
        return null;
    }

    @Override
    public StructType getTableSchema(String tableName) {
        return null;
    }
}
