package org.nanodb.backend.primitives;

public interface DataType<T> {
    // basically, serialize and deserialize
    T fromBytes(byte[] bytes, int offset);
    byte[] toBytes(T data);
    int byteSize();
}