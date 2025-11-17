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
        byte[] bytes = type.toBytes(true);
        assertEquals(1, bytes.length);
        assertEquals(1, bytes[0]);
    }

    @Test
    public void testToBytesFalse() {
        byte[] bytes = type.toBytes(false);
        assertEquals(1, bytes.length);
        assertEquals(0, bytes[0]);
    }

    @Test
    public void testFromBytesTrue() {
        byte[] bytes = { 1 };
        assertTrue(type.fromBytes(bytes, 0));
    }

    @Test
    public void testFromBytesFalse() {
        byte[] bytes = { 0 };
        assertFalse(type.fromBytes(bytes, 0));
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
        // TestDataTypeVisitor returns null for BooleanType, so for coverage, check null
        assertNull(result);
    }
}
