package org.nanodb.db;

import org.nanodb.chunker.Chunker;

// Should represent the data structure representing the database
public abstract class Db {
    protected Chunker chunker;
    public Db(Chunker chunker) { this.chunker = chunker; }
}
