package org.nanodb.internal.primitives;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BytesTypeTest {
    @Test
    public void testBytesTypeRoundTrip() {
        BytesType t = new BytesType(8);
        byte[] value = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        byte[] bytes = t.toBytes(value);
        assertArrayEquals(value, bytes);
        byte[] result = t.fromBytes(bytes, 0);
        assertArrayEquals(value, result);
        assertEquals(8, t.byteSize());
        assertEquals(8, t.getLength());
    }

    @Test
    public void testBytesTypeShortInput() {
        BytesType t = new BytesType(8);
        byte[] value = new byte[] { 1, 2, 3 };
        byte[] bytes = t.toBytes(value);
        // Should not pad, just return input as is
        assertArrayEquals(value, bytes);
        // fromBytes expects 8 bytes, so pad manually for test
        byte[] padded = new byte[8];
        System.arraycopy(value, 0, padded, 0, value.length);
        byte[] result = t.fromBytes(padded, 0);
        assertArrayEquals(padded, result);
    }

    @Test
    public void testBytesTypeLongInput() {
        BytesType t = new BytesType(4);
        byte[] value = new byte[] { 9, 8, 7, 6, 5, 4 };
        byte[] bytes = t.toBytes(value);
        // Should not truncate, just return input as is
        assertArrayEquals(value, bytes);
        // fromBytes only reads 4 bytes
        byte[] result = t.fromBytes(bytes, 0);
        assertArrayEquals(new byte[] { 9, 8, 7, 6 }, result);
    }

    @Test
    public void testBytesTypeFromBytesWithOffset() {
        BytesType t = new BytesType(3);
        byte[] value = new byte[] { 11, 22, 33 };
        byte[] bigger = new byte[10];
        System.arraycopy(value, 0, bigger, 5, value.length);
        byte[] result = t.fromBytes(bigger, 5);
        assertArrayEquals(value, result);
    }

    @Test
    public void testBytesTypeAcceptVisitor() {
        BytesType t = new BytesType(2);
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = t.accept(visitor);
        assertEquals("visited", result);
    }

    @Test
    public void testBytesTypeNullInput() {
        BytesType t = new BytesType(2);
        // toBytes should return null if input is null
        byte[] result = t.toBytes(null);
        assertEquals(null, result);
        // fromBytes should throw NullPointerException if bytes is null
        try {
            t.fromBytes(null, 0);
            throw new AssertionError("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testBytesTypeFromBytesOffsetOutOfBounds() {
        BytesType t = new BytesType(2);
        byte[] bytes = new byte[] { 1 };
        try {
            t.fromBytes(bytes, 1); // offset + length > bytes.length
            throw new AssertionError("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
}
