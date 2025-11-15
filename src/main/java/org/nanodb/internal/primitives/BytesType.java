package org.nanodb.internal.primitives;

public class BytesType implements DataType<byte[]> {
    private final int length;

    public BytesType(int length) {
        this.length = length;
    }

    public int getLength() {return length;}

    @Override
    public byte[] fromBytes(byte[] bytes, int offset) {
        byte[] byteArray = new byte[length]; // Create a byte array of specified length

        // Copy the string bytes to the byte array, respecting the specified length
        System.arraycopy(bytes, offset, byteArray, 0, length);

        return byteArray;
    }

    @Override
    public byte[] toBytes(byte[] data) {
        return data;
    }

    @Override
    public int byteSize() {
        return length; // Fixed size
    }


    @Override
    public <T> T accept(DataTypeVisitor<T> visitor) {return visitor.visit(this);}
}
