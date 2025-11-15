package org.nanodb.internal.primitives;

import java.util.ArrayList;
import java.util.List;

import org.nanodb.utils.ByteExtensions;

public class ArrayType<T> implements DataType<List<T>> {
    private final DataType<T> elementType;
    private final int length;

    public ArrayType(DataType<T> elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    public DataType<T> getElementType() {return elementType;}
    public int getLength() {return length;}

    @Override
    public List<T> fromBytes(byte[] bytes, int offset) {
        List<T> array = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            array.add(elementType.fromBytes(bytes, offset));
            offset += elementType.byteSize();
        }
        return array;
    }

    @Override
    public byte[] toBytes(List<T> data) {
        List<byte[]> bytes = new ArrayList<>();

        for (T element : data) {
            byte[] elementData = elementType.toBytes(element);
            bytes.add(elementData);
        }
        return ByteExtensions.concat(bytes);
    }

    @Override
    public int byteSize() {
        return length * elementType.byteSize();
    }


    @Override
    public <T2> T2 accept(DataTypeVisitor<T2> visitor) {return visitor.visit(this);}
}