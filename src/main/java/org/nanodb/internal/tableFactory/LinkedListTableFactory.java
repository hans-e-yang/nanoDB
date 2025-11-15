package org.nanodb.internal.tableFactory;

import org.nanodb.internal.chunker.Chunker;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.internal.table.LinkedListTable;
import org.nanodb.utils.Table;

public class LinkedListTableFactory extends TableFactory{
    public LinkedListTableFactory(Chunker chunker) {
        super(chunker);
    }

    public InternalTable createNew(StructType rowType) {
        return new LinkedListTable(this.chunker, rowType);
    }
    public InternalTable get(int chunkId, StructType rowType) {
        return new LinkedListTable(this.chunker, chunkId, rowType);
    }
}
