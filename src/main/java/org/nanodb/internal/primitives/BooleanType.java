package org.nanodb.internal.primitives;

public class BooleanType implements DataType<Boolean> {
    @Override
    public Boolean fromBytes(byte[] bytes, int offset) {
        return bytes[offset] == 1;
    }

    @Override
    public byte[] toBytes(Boolean data) {
        byte[] bytes = new byte[1]; // Create a byte array of length 4
        bytes[0] = (byte) (data ? 1 : 0);
        return bytes;
    }

    @Override
    public int byteSize() {
        return 1; // 4 bytes for Int32
    }

    @Override
    public <T> T accept(DataTypeVisitor<T> visitor) {return visitor.visit(this);}
}