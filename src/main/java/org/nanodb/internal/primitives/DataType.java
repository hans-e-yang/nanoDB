package org.nanodb.internal.primitives;

public interface DataType<T> {
    // basically, serialize and deserialize
    T fromBytes(byte[] bytes, int offset);
    byte[] toBytes(T data);
    int byteSize();

    <T2> T2 accept(DataTypeVisitor<T2> visitor);
}