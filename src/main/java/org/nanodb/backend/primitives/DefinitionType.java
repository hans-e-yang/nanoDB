package org.nanodb.backend.primitives;

import org.nanodb.backend.primitives.serializer.DataTypeSerializer;

/**
 * Special data type used to store a DataType object itself as a byte[]
 */
public class DefinitionType implements DataType<DataType> {
    private final int length;

    public DefinitionType(int length) {
        this.length = length;
    }

    public int getLength() {return length;}

    @Override
    public DataType fromBytes(byte[] bytes, int offset) {
        return DataTypeSerializer.getInstance().serialize(bytes, offset);
    }

    @Override
    public byte[] toBytes(DataType data) {
        return DataTypeSerializer.getInstance().deserialize(data);
    }

    @Override
    public int byteSize() {
        return length; // Fixed size
    }


    @Override
    public <T> T accept(DataTypeVisitor<T> visitor) {return visitor.visit(this);}
}
