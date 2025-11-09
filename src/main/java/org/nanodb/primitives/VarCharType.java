package org.nanodb.primitives;

import java.nio.charset.StandardCharsets;

public class VarCharType implements DataType<String> {
    private final int length;

    public VarCharType(int length) {
        this.length = length;
    }

    @Override
    public String fromBytes(byte[] bytes, int offset) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8).trim(); // Convert bytes to String
    }

    @Override
    public byte[] toBytes(String data) {
        byte[] byteArray = new byte[length]; // Create a byte array of specified length

        // Convert the String to bytes
        byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);

        // Copy the string bytes to the byte array, respecting the specified length
        int bytesToCopy = Math.min(stringBytes.length, length);
        System.arraycopy(stringBytes, 0, byteArray, 0, bytesToCopy);

        // If the string is shorter than the specified length, the rest will be filled with 0s
        return byteArray;
    }

    @Override
    public int byteSize() {
        return length; // Fixed size
    }
}
