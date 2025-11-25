package org.nanodb.internal.primitives;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BytesTypeTest {

    @Test
    public void testBytesTypeRoundTrip() {
        BytesType type = new BytesType(5);
        byte[] original = { 10, 20, 30, 40, 50 };
        byte[] serialized = type.toBytes(original);
        byte[] deserialized = type.fromBytes(serialized, 0);

        assertArrayEquals(original, deserialized);
        assertEquals(type.byteSize(), serialized.length);
    }

    @Test
    public void testFromBytesWithOffset() {
        BytesType type = new BytesType(3);
        byte[] original = { 1, 2, 3, 4, 5 };
        byte[] deserialized = type.fromBytes(original, 2);

        assertArrayEquals(new byte[] { 3, 4, 5 }, deserialized);
    }

    @Test
    public void testByteSize() {
        BytesType type = new BytesType(8);
        assertEquals(8, type.byteSize());
    }

    @Test
    public void testFromBytesOffsetOutOfBounds() {
        BytesType type = new BytesType(2);
        byte[] bytes = { 1 };
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> type.fromBytes(bytes, 1));
    }
}
