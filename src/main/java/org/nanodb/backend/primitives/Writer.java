package org.nanodb.backend.primitives;

import org.nanodb.utils.ByteExtensions;

import java.util.ArrayList;
import java.util.List;

public class Writer {
    private final List<byte[]> data = new ArrayList<>();
    public Writer() {}

    public <T> void write(DataType<T> type, T data) {
        this.data.add(type.toBytes(data));
    }

    public void write(byte[] data) {this.data.add(data); }

    public byte[] array() {
        return ByteExtensions.concat(data);
    }
}
