package org.nanodb.backend.primitives.serializer;

import java.util.Arrays;
import java.util.List;

import org.nanodb.backend.primitives.ArrayType;
import org.nanodb.backend.primitives.DataType;
import org.nanodb.backend.primitives.DataTypeVisitor;
import org.nanodb.backend.primitives.DateType;
import org.nanodb.backend.primitives.Int32Type;
import org.nanodb.backend.primitives.StructType;
import org.nanodb.backend.primitives.VarCharType;

public class ByteSerializer<T> implements Serializer<byte[], T>{
    public byte[] deserialize(DataType<T> type, T value) {
        return type.toBytes(value);

    }
    public T serialize(DataType<T> type, List<Byte> value) {

    }
}
