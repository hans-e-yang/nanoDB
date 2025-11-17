package org.nanodb.internal.primitives;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTypeTest {
    @Test
    public void testRoundTripAndByteSize() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 3);
        List<Integer> data = Arrays.asList(1, 2, 3);
        byte[] bytes = arrType.toBytes(data);
        List<Integer> result = arrType.fromBytes(bytes, 0);
        assertEquals(data, result);
        assertEquals(3 * elt.byteSize(), arrType.byteSize());
    }

    @Test
    public void testShortListToBytes() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 4);
        List<Integer> shortData = Arrays.asList(10, 20);
        byte[] bytes = arrType.toBytes(shortData);
        assertEquals(shortData.size() * elt.byteSize(), bytes.length);
    }

    @Test
    public void testFromBytesWithOffset() {
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
    public void testGetters() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 5);
        assertSame(elt, arrType.getElementType());
        assertEquals(5, arrType.getLength());
    }

    @Test
    public void testVisitorPattern() {
        ArrayType<Integer> arrType = new ArrayType<>(new Int32Type(), 2);
        TestDataTypeVisitor visitor = new TestDataTypeVisitor();
        String result = arrType.accept(visitor);
        assertEquals("visited", result);
    }

    @Test
    public void testEdgeCaseEmptyArray() {
        Int32Type elt = new Int32Type();
        ArrayType<Integer> arrType = new ArrayType<>(elt, 0);
        List<Integer> empty = new ArrayList<>();
        byte[] bytes = arrType.toBytes(empty);
        assertEquals(0, bytes.length);
        List<Integer> result = arrType.fromBytes(bytes, 0);
        assertEquals(empty, result);
    }
}
