package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTypeTest {
    @Test
    public void testArrayTypeRoundTrip() {
        BooleanType booleanType = new BooleanType();
        ArrayType<Boolean> arrayType = new ArrayType<>(booleanType, 3);

        List<Boolean> original = Arrays.asList(true, false, true);
        byte[] serialized = arrayType.toBytes(original);
        List<Boolean> deserialized = arrayType.fromBytes(serialized, 0);

        assertEquals(original, deserialized);
        assertEquals(arrayType.byteSize(), serialized.length);
    }

    @Test
    public void testArrayTypeEmptyArray() {
        BooleanType booleanType = new BooleanType();
        ArrayType<Boolean> arrayType = new ArrayType<>(booleanType, 0);

        List<Boolean> original = Arrays.asList();
        byte[] serialized = arrayType.toBytes(original);
        List<Boolean> deserialized = arrayType.fromBytes(serialized, 0);

        assertEquals(original, deserialized);
        assertEquals(arrayType.byteSize(), serialized.length);
    }

    @Test
    public void testArrayTypeByteSize() {
        BooleanType booleanType = new BooleanType();
        ArrayType<Boolean> arrayType = new ArrayType<>(booleanType, 5);

        assertEquals(5, arrayType.getLength());
        assertEquals(5 * booleanType.byteSize(), arrayType.byteSize());
    }

    @Test
    public void testArrayTypeFromBytesOffsetOutOfBounds() {
        BooleanType booleanType = new BooleanType();
        ArrayType<Boolean> arrayType = new ArrayType<>(booleanType, 3);
        byte[] bytes = new byte[] { 1, 0 }; // Not enough bytes for 3 elements

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> arrayType.fromBytes(bytes, 0));
    }
}
