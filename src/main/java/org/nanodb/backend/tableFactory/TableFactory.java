package org.nanodb.backend.tableFactory;

import org.nanodb.backend.chunker.Chunker;
import org.nanodb.backend.primitives.StructType;
import org.nanodb.backend.table.Table;

public abstract class TableFactory {
    protected Chunker chunker;
    public TableFactory(Chunker chunker) {
        this.chunker = chunker;
    }

    public abstract Table createNew(StructType rowType);
    public abstract Table get(int chunkId, StructType rowType);
    public Table getInitial(StructType rowType) {
        return get(chunker.getInitialChunkId(), rowType);
    }
}
