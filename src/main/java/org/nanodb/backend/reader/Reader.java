package org.nanodb.backend.reader;

import org.nanodb.backend.primitives.DataType;

public class Reader {
    private final byte[] data;
    private int offset = 0;
    public Reader(byte[] data) {
        this.data = data;
    }

    public <T> T Read(DataType<T> type) {
        if (this.offset + type.byteSize() > this.data.length) {
            throw new IllegalArgumentException("Invalid Reading beyond chunk");
        }
        T data = type.fromBytes(this.data, this.offset);
        this.offset += type.byteSize();
        return data;
    }
}
