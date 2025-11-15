package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PrimitiveTypesTest {

    @Test
    public void testInt32TypeRoundTrip() {
        Int32Type t = new Int32Type();
        Integer value = 100;
        byte[] bytes = t.toBytes(value);
        Integer result = t.fromBytes(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    public void testVarCharTypeRoundTrip() {
        VarCharType t = new VarCharType(10);
        String value = "hello";
        byte[] bytes = t.toBytes(value);
        String result = t.fromBytes(bytes, 0);
        assertEquals(value, result);
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
    public void testDateTypeRoundTrip() {
        DateType t = new DateType();
        Date now = new Date();
        byte[] bytes = t.toBytes(now);
        Date result = t.fromBytes(bytes, 0);
        assertEquals(now.getTime(), result.getTime());
    }

    @Test
    public void testArrayTypeRoundTrip() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 3);
        List<Integer> data = Arrays.asList(1, 2, 3);
        byte[] bytes = arrType.toBytes(data);
        List<Integer> result = arrType.fromBytes(bytes, 0);
        assertEquals(data, result);
    }

    @Test
    public void testArrayTypeByteSizeAndShortList() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 4);
        // byteSize should be length * element.byteSize()
        assertEquals(4 * elt.byteSize(), arrType.byteSize());

        // If the provided list has fewer elements than length, toBytes should only
        // encode the provided elements
        List<Integer> shortData = Arrays.asList(10, 20);
        byte[] bytes = arrType.toBytes(shortData);
        assertEquals(shortData.size() * elt.byteSize(), bytes.length);
    }

    @Test
    public void testArrayTypeFromBytesWithOffset() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 3);
        List<Integer> data = Arrays.asList(7, 8, 9);
        byte[] bytes = arrType.toBytes(data);

        byte[] bigger = new byte[bytes.length + 6];
        Arrays.fill(bigger, (byte) 0x5A);
        System.arraycopy(bytes, 0, bigger, 4, bytes.length);

        List<Integer> result = arrType.fromBytes(bigger, 4);
        assertEquals(data, result);
    }

    @Test
    public void testStructTypeRoundTrip() {
        // fields: id:int, name:varchar(10)
        List<org.nanodb.utils.Pair<String, DataType<?>>> fields = Arrays.asList(
                new org.nanodb.utils.Pair<>("id", new Int32Type()),
                new org.nanodb.utils.Pair<>("name", new VarCharType(10)));
        StructType struct = new StructType(fields);

        Map<String, Object> row = new HashMap<>();
        row.put("id", 42);
        row.put("name", "Alice");

        byte[] bytes = struct.toBytes(row);
        Map<String, Object> result = struct.fromBytes(bytes, 0);

        assertEquals(42, ((Integer) result.get("id")).intValue());
        assertEquals("Alice", result.get("name"));
    }

    @Test
    public void testStructTypeInvalidValueType() {
        // fields: id:int, name:varchar(10)
        List<org.nanodb.utils.Pair<String, DataType<?>>> fields = Arrays.asList(
                new org.nanodb.utils.Pair<>("id", new Int32Type()),
                new org.nanodb.utils.Pair<>("name", new VarCharType(10)));
        StructType struct = new StructType(fields);

        Map<String, Object> row = new HashMap<>();
        row.put("id", 42);
        // wrong type for "name" (should be String)
        row.put("name", 12345);

        // Expect IllegalArgumentException due to ClassCastException inside toBytes
        assertThrows(IllegalArgumentException.class, () -> struct.toBytes(row));
    }

    @Test
    public void testByteSizeAndGetFields() {
        List<org.nanodb.utils.Pair<String, DataType<?>>> fields = Arrays.asList(
                new org.nanodb.utils.Pair<>("a", new Int32Type()),
                new org.nanodb.utils.Pair<>("b", new VarCharType(8)),
                new org.nanodb.utils.Pair<>("c", new DateType()));
        StructType struct = new StructType(fields);

        int expectedSize = 4 + 8 + 8; // Int32 + VarChar(8) + Date(8)
        assertEquals(expectedSize, struct.byteSize());
        assertEquals(fields, struct.getFields());
    }

    @Test
    public void testFromBytesWithOffset() {
        List<org.nanodb.utils.Pair<String, DataType<?>>> fields = Arrays.asList(
                new org.nanodb.utils.Pair<>("id", new Int32Type()),
                new org.nanodb.utils.Pair<>("name", new VarCharType(6)));
        StructType struct = new StructType(fields);

        Map<String, Object> row = new HashMap<>();
        row.put("id", 7);
        row.put("name", "Bob");

        byte[] structBytes = struct.toBytes(row);

        // create a bigger array and copy structBytes at offset 5
        byte[] bigger = new byte[structBytes.length + 10];
        for (int i = 0; i < bigger.length; i++)
            bigger[i] = (byte) 0x7F; // filler
        System.arraycopy(structBytes, 0, bigger, 5, structBytes.length);

        Map<String, Object> result = struct.fromBytes(bigger, 5);
        assertEquals(7, ((Integer) result.get("id")).intValue());
        assertEquals("Bob", result.get("name"));
    }

}
