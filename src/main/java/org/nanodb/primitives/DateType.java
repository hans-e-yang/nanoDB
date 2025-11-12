package org.nanodb.primitives;

import java.util.Date;

public class DateType implements DataType<Date> {
    @Override
    public Date fromBytes(byte[] bytes, int offset) {
        // Convert 8 bytes to long (timestamp in milliseconds)
        long timestamp = ((long) (bytes[offset] & 0xFF) << 56) |
                        ((long) (bytes[offset + 1] & 0xFF) << 48) |
                        ((long) (bytes[offset + 2] & 0xFF) << 40) |
                        ((long) (bytes[offset + 3] & 0xFF) << 32) |
                        ((long) (bytes[offset + 4] & 0xFF) << 24) |
                        ((long) (bytes[offset + 5] & 0xFF) << 16) |
                        ((long) (bytes[offset + 6] & 0xFF) << 8) |
                        ((long) (bytes[offset + 7] & 0xFF));
        return new Date(timestamp);
    }

    @Override
    public byte[] toBytes(Date data) {
        long timestamp = data.getTime();
        byte[] bytes = new byte[8];
        bytes[0] = (byte) ((timestamp >> 56) & 0xFF);
        bytes[1] = (byte) ((timestamp >> 48) & 0xFF);
        bytes[2] = (byte) ((timestamp >> 40) & 0xFF);
        bytes[3] = (byte) ((timestamp >> 32) & 0xFF);
        bytes[4] = (byte) ((timestamp >> 24) & 0xFF);
        bytes[5] = (byte) ((timestamp >> 16) & 0xFF);
        bytes[6] = (byte) ((timestamp >> 8) & 0xFF);
        bytes[7] = (byte) (timestamp & 0xFF);
        return bytes;
    }

    @Override
    public int byteSize() {
        return 8; // 8 bytes for Date (timestamp as long)
    }
}
