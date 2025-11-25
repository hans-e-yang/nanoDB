package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VarCharTypeTest {
    private final VarCharType type = new VarCharType(10);

    @Test
    public void testToBytes() {
        String original = "hello";
        byte[] bytes = type.toBytes(original);
        String afterSerialization = type.fromBytes(bytes, 0);
        assertEquals(original, afterSerialization);
        assertEquals(type.byteSize(), bytes.length);
    }

    @Test
    public void testFromBytes() {
        byte[] bytes = "world".getBytes();
        byte[] paddedBytes = new byte[10];
        System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);

        String value = type.fromBytes(paddedBytes, 0);
        byte[] serialized = type.toBytes(value);
        assertArrayEquals(paddedBytes, serialized);
        assertEquals("world", value);
    }

    @Test
    public void testTruncation() {
        VarCharType shortType = new VarCharType(5);
        String longString = "abcdefgh";
        byte[] bytes = shortType.toBytes(longString);
        String result = shortType.fromBytes(bytes, 0);
        assertEquals("abcde", result);
    }

    @Test
    public void testFromBytesWithOffset() {
        byte[] largerArray = new byte[20];
        String data = "test";
        byte[] dataBytes = type.toBytes(data);
        System.arraycopy(dataBytes, 0, largerArray, 5, dataBytes.length);

        String result = type.fromBytes(largerArray, 5);
        assertEquals("test", result);
    }

    @Test
    public void testFromBytesNegativeOffset() {
        byte[] bytes = new byte[10];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> type.fromBytes(bytes, -1));
    }

    @Test
    public void testFromBytesOffsetOutOfBounds() {
        byte[] bytes = new byte[5];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> type.fromBytes(bytes, 0));
    }

    @Test
    public void testGetLength() {
        assertEquals(10, type.getLength());
    }

    @Test
    public void testByteSize() {
        assertEquals(10, type.byteSize());
    }

    @Test
    public void testVisitorPattern() {
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = type.accept(visitor);
        assertEquals("visited", result);
    }
}
