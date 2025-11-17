package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VarCharTypeTest {
    @Test
    public void testVarCharTypeRoundTrip() {
        VarCharType t = new VarCharType(10);
        String value = "hello";
        byte[] bytes = t.toBytes(value);
        String result = t.fromBytes(bytes, 0);
        assertEquals(value, result);
        assertEquals(10, t.getLength());
        assertEquals(10, t.byteSize());
    }

    @Test
    public void testVarCharTypeRoundTripWithCapitals() {
        VarCharType t = new VarCharType(10);
        String value = "Hello";
        byte[] bytes = t.toBytes(value);
        String result = t.fromBytes(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    public void testVarCharTypeTruncate() {
        VarCharType t = new VarCharType(5);
        String value = "abcdefgh"; // longer than 5
        byte[] bytes = t.toBytes(value);
        String result = t.fromBytes(bytes, 0);
        // toBytes should truncate to first 5 chars
        assertEquals("abcde", result);
    }

    @Test
    public void testVarCharTypeAcceptVisitor() {
        VarCharType t = new VarCharType(3);
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = t.accept(visitor);
        assertEquals("visited", result);
    }

    @Test
    public void testVarCharTypeNullInput() {
        VarCharType t = new VarCharType(5);
        // toBytes should throw NullPointerException if input is null
        try {
            t.toBytes(null);
            throw new AssertionError("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testVarCharTypeFromBytesOffsetOutOfBounds() {
        VarCharType t = new VarCharType(5);
        byte[] bytes = new byte[2]; // too short
        try {
            t.fromBytes(bytes, 0);
            throw new AssertionError("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
}
