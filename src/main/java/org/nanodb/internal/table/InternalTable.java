package org.nanodb.internal.table;

import org.nanodb.internal.primitives.StructType;
import org.nanodb.utils.Table;

import java.util.Map;

public abstract class InternalTable extends Table<Map<String, Object>> {
    public abstract StructType getSchema();
    public abstract int getInitialId();
}
