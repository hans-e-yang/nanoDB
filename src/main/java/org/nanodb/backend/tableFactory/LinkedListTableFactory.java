package org.nanodb.backend.tableFactory;

import org.nanodb.backend.chunker.Chunker;
import org.nanodb.backend.primitives.StructType;
import org.nanodb.backend.table.LinkedListTable;
import org.nanodb.backend.table.Table;

public class LinkedListTableFactory extends TableFactory{
    public LinkedListTableFactory(Chunker chunker) {
        super(chunker);
    }

    public Table createNew(StructType rowType) {
        return new LinkedListTable(this.chunker, rowType);
    }
    public Table get(int chunkId, StructType rowType) {
        return new LinkedListTable(this.chunker, chunkId, rowType);
    }
}
