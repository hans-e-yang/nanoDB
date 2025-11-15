package org.nanodb.internal.tableFactory;

import org.nanodb.internal.chunker.Chunker;
import org.nanodb.internal.primitives.StructType;
import org.nanodb.internal.table.InternalTable;
import org.nanodb.utils.Table;

public abstract class TableFactory {
    protected Chunker chunker;
    public TableFactory(Chunker chunker) {
        this.chunker = chunker;
    }

    public abstract InternalTable createNew(StructType rowType);
    public abstract InternalTable get(int chunkId, StructType rowType);
    public InternalTable getInitial(StructType rowType) {
        return get(chunker.getInitialChunkId(), rowType);
    }
}
