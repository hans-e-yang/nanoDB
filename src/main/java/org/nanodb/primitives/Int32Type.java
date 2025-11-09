package org.nanodb.primitives;

public class Int32Type implements DataType<Integer> {
    @Override
    public Integer fromBytes(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);
    }

    @Override
    public byte[] toBytes(Integer data) {
        byte[] bytes = new byte[4]; // Create a byte array of length 4
        bytes[0] = (byte) ((data >> 24) & 0xFF); // Highest byte
        bytes[1] = (byte) ((data >> 16) & 0xFF);
        bytes[2] = (byte) ((data >> 8) & 0xFF);
        bytes[3] = (byte) (data & 0xFF); // Lowest byte
        return bytes;
    }

    @Override
    public int byteSize() {
        return 4; // 4 bytes for Int32
    }
}