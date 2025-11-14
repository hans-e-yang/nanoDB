package org.nanodb.backend.primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayType<T> implements DataType<List<T>> {
    private final DataType<T> elementType;
    private final int length;

    public ArrayType(DataType<T> elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            for (T element : data) {
                byte[] bytes = elementType.toBytes(element);
                outputStream.write(bytes);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            return new byte[]{};
        }
    }

    @Override
    public int byteSize() {
        return length * elementType.byteSize();
    }
}