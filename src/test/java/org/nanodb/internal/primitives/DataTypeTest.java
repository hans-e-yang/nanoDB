package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataType interface through concrete implementations
 * Minimal test case with 100% coverage of interface contract
 */
public class DataTypeTest {

    @Test
    public void testDataTypeContract() {
        // Test all DataType interface methods through BooleanType
        BooleanType type = new BooleanType();
        boolean original = true;

        // Test toBytes
        byte[] serialized = type.toBytes(original);

        // Test fromBytes and byteSize
        boolean deserialized = type.fromBytes(serialized, 0);
        assertEquals(original, deserialized);
        assertEquals(type.byteSize(), serialized.length);

        // Test accept (visitor pattern)
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = type.accept(visitor);
        assertEquals("visited", result);
    }
}