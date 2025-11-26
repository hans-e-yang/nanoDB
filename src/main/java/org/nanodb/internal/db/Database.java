package org.nanodb.internal.db;

import org.nanodb.internal.exception.MissingWhereConditionException;
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;
import org.nanodb.internal.primitives.*;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.utils.serializer.DataTypeSerializer;
import org.nanodb.utils.Table;
import org.nanodb.internal.tableFactory.TableFactory;
import org.nanodb.utils.Pair;

import java.util.*;

public class Database extends Db {
    private final Map<String, InternalTable> tables = new HashMap<>();
    private final InternalTable metadata;

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
        List<Map<String, Object>> tables;
        try {
            tables = this.metadata.selectAll();
        } catch (MissingWhereConditionException e) {
            throw new RuntimeException(e);
        }
        // Load the table definitions
        for (Map<String, Object> table : tables) {
            String tableName = (String) table.get(NAME_FIELD);
            Integer tableBlockId = (Integer) table.get(PTR_FIELD);
            StructType tableSchema = (StructType) DataTypeSerializer.getInstance().serialize((byte[]) table.get(DEFINITION_FIELD));
            if (this.tables.containsKey(tableName)) {
                throw new TableFileCorruptedException("Duplicate table names exist!");
            }

            this.tables.put(tableName, this.tableFactory.get(tableBlockId, tableSchema));
        }

    }

    @Override
    public boolean createTable(String tableName, StructType schema) {
        if (tables.containsKey(tableName)) return false;
        InternalTable newTable = this.tableFactory.createNew(schema);

        // Write to metadata
        Map<String, Object> tableDefinition = new HashMap<>();
        tableDefinition.put(NAME_FIELD, tableName);
        tableDefinition.put(PTR_FIELD, newTable.getInitialId());
        tableDefinition.put(DEFINITION_FIELD, DataTypeSerializer.getInstance().deserialize(schema));
        metadata.insert(tableDefinition);

        // Also add to memory
        tables.put(tableName, newTable);
        return true;
    }

    @Override
    public boolean dropTable(String tableName) {
        if (!tables.containsKey(tableName)) return false;
        // clear table
        InternalTable table = this.tables.get(tableName);
        table.clear();

        // remove from metadata where name equal tablename
        try {
            metadata.delete(new ComparisonCondition(NAME_FIELD, ComparisonOperation.EQUAL, tableName));
        } catch (MissingWhereConditionException e) {
            throw new RuntimeException(e);
        }

        // remove from memory too
        this.tables.remove(tableName);

        return true;
    }

    @Override
    public InternalTable getTable(String tableName) {
        return this.tables.get(tableName);
    }

    @Override
    public List<String> getAllTables() {
        return this.tables.keySet().stream().toList();
    }

    @Override
    public StructType getTableSchema(String tableName) {
        InternalTable table = this.tables.get(tableName);
        if (table != null) return table.getSchema();
        else return null;
    }
}
