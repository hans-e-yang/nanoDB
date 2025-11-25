package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BooleanType
 * Coverage: All branches, edge cases, and visitor pattern
 */
public class BooleanTypeTest {
    private final BooleanType type = new BooleanType();

    @Test
    public void testToBytesTrue() {
        boolean original = true;
        byte[] bytes = type.toBytes(original);
        boolean afterSerialization = type.fromBytes(bytes, 0);
        assertEquals(original, afterSerialization);
        assertEquals(type.byteSize(), bytes.length);
    }

    @Test
    public void testToBytesFalse() {
        boolean original = false;
        byte[] bytes = type.toBytes(original);
        boolean afterSerialization = type.fromBytes(bytes, 0);
        assertEquals(original, afterSerialization);
        assertEquals(type.byteSize(), bytes.length);
    }

    @Test
    public void testFromBytesTrue() {
        byte[] bytes = { 1 };
        boolean value = type.fromBytes(bytes, 0);
        byte[] serialized = type.toBytes(value);
        assertArrayEquals(bytes, serialized);
        assertTrue(value);
    }

    @Test
    public void testFromBytesFalse() {
        byte[] bytes = { 0 };
        boolean value = type.fromBytes(bytes, 0);
        byte[] serialized = type.toBytes(value);
        assertArrayEquals(bytes, serialized);
        assertFalse(value);
    }

    @Test
    public void testFromBytesNonZeroIsFalse() {
        byte[] bytes = { 42 }; // any non-1 value should be false
        assertFalse(type.fromBytes(bytes, 0));
    }

    @Test
    public void testByteSize() {
        assertEquals(1, type.byteSize());
    }

    @Test
    public void testVisitorPattern() {
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = type.accept(visitor);
        assertEquals("visited", result);
    }
}
