package org.nanodb.internal.primitives.visitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Pair;
import java.util.Arrays;

public class DataTypeComparerTest {

    private DateType dateType;
    private Int32Type intType;
    private BooleanType boolType;
    private VarCharType varchar10;
    private VarCharType varchar20;
    private BytesType bytes5;
    private BytesType bytes10;

    @BeforeEach
    void setUp() {
        dateType = new DateType();
        intType = new Int32Type();
        boolType = new BooleanType();
        varchar10 = new VarCharType(10);
        varchar20 = new VarCharType(20);
        bytes5 = new BytesType(5);
        bytes10 = new BytesType(10);
    }

    @Test
    void testPrimitiveTypesEqual() {
        assertTrue(DataTypeComparer.compare(dateType, new DateType()));
        assertTrue(DataTypeComparer.compare(intType, new Int32Type()));
        assertTrue(DataTypeComparer.compare(boolType, new BooleanType()));
    }

    @Test
    void testPrimitiveTypesDifferent() {
        assertFalse(DataTypeComparer.compare(dateType, intType));
        assertFalse(DataTypeComparer.compare(intType, boolType));
        assertFalse(DataTypeComparer.compare(boolType, dateType));
    }

    @Test
    void testVarCharTypesWithSameLength() {
        assertTrue(DataTypeComparer.compare(varchar10, new VarCharType(10)));
    }

    @Test
    void testVarCharTypesWithDifferentLength() {
        assertFalse(DataTypeComparer.compare(varchar10, varchar20));
    }

    @Test
    void testBytesTypesWithSameLength() {
        assertTrue(DataTypeComparer.compare(bytes5, new BytesType(5)));
    }

    @Test
    void testBytesTypesWithDifferentLength() {
        assertFalse(DataTypeComparer.compare(bytes5, bytes10));
    }

    @Test
    void testArrayTypesEqual() {
        ArrayType<?> array1 = new ArrayType<>(intType, 3);
        ArrayType<?> array2 = new ArrayType<>(new Int32Type(), 3);
        assertTrue(DataTypeComparer.compare(array1, array2));
    }

    @Test
    void testArrayTypesDifferentLength() {
        ArrayType<?> array1 = new ArrayType<>(intType, 3);
        ArrayType<?> array2 = new ArrayType<>(intType, 5);
        assertFalse(DataTypeComparer.compare(array1, array2));
    }

    @Test
    void testArrayTypesDifferentElementType() {
        ArrayType<?> intArray = new ArrayType<>(intType, 3);
        ArrayType<?> boolArray = new ArrayType<>(boolType, 3);
        assertFalse(DataTypeComparer.compare(intArray, boolArray));
    }

    @Test
    void testNestedArrayTypes() {
        ArrayType<?> innerArray1 = new ArrayType<>(intType, 2);
        ArrayType<?> innerArray2 = new ArrayType<>(new Int32Type(), 2);
        ArrayType<?> outerArray1 = new ArrayType<>(innerArray1, 3);
        ArrayType<?> outerArray2 = new ArrayType<>(innerArray2, 3);

        assertTrue(DataTypeComparer.compare(outerArray1, outerArray2));
    }

    @Test
    void testStructTypesEqual() {
        StructType struct1 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10),
                new Pair<>("active", boolType)));

        StructType struct2 = new StructType(Arrays.asList(
                new Pair<>("id", new Int32Type()),
                new Pair<>("name", new VarCharType(10)),
                new Pair<>("active", new BooleanType())));

        assertTrue(DataTypeComparer.compare(struct1, struct2));
    }

    @Test
    void testStructTypesDifferentFieldCount() {
        StructType struct1 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10)));

        StructType struct2 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10),
                new Pair<>("active", boolType)));

        assertFalse(DataTypeComparer.compare(struct1, struct2));
    }

    @Test
    void testStructTypesDifferentFieldNames() {
        StructType struct1 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10)));

        StructType struct2 = new StructType(Arrays.asList(
                new Pair<>("userId", intType),
                new Pair<>("name", varchar10)));

        assertFalse(DataTypeComparer.compare(struct1, struct2));
    }

    @Test
    void testStructTypesDifferentFieldTypes() {
        StructType struct1 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10)));

        StructType struct2 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar20)));

        assertFalse(DataTypeComparer.compare(struct1, struct2));
    }

    @Test
    void testStructTypesWithFieldOrderMatters() {
        StructType struct1 = new StructType(Arrays.asList(
                new Pair<>("name", varchar10),
                new Pair<>("id", intType)));

        StructType struct2 = new StructType(Arrays.asList(
                new Pair<>("id", intType),
                new Pair<>("name", varchar10)));

        assertFalse(DataTypeComparer.compare(struct1, struct2));
    }

    @Test
    void testComplexNestedStructure() {
        // Array of structs containing arrays
        ArrayType<?> tagArray = new ArrayType<>(varchar10, 3);
        StructType innerStruct = new StructType(Arrays.asList(
                new Pair<>("tags", tagArray),
                new Pair<>("count", intType)));

        ArrayType<?> arrayOfStructs1 = new ArrayType<>(innerStruct, 5);

        ArrayType<?> tagArray2 = new ArrayType<>(new VarCharType(10), 3);
        StructType innerStruct2 = new StructType(Arrays.asList(
                new Pair<>("tags", tagArray2),
                new Pair<>("count", new Int32Type())));

        ArrayType<?> arrayOfStructs2 = new ArrayType<>(innerStruct2, 5);

        assertTrue(DataTypeComparer.compare(arrayOfStructs1, arrayOfStructs2));
    }

    @Test
    void testSameInstanceReturnsTrue() {
        assertTrue(DataTypeComparer.compare(intType, intType));
        assertTrue(DataTypeComparer.compare(varchar10, varchar10));
    }

    @Test
    void testArrayVsNonArrayType() {
        ArrayType<?> intArray = new ArrayType<>(intType, 3);
        assertFalse(DataTypeComparer.compare(intArray, intType));
        assertFalse(DataTypeComparer.compare(intType, intArray));
    }

    @Test
    void testStructVsNonStructType() {
        StructType struct = new StructType(Arrays.asList(
                new Pair<>("id", intType)));
        assertFalse(DataTypeComparer.compare(struct, intType));
        assertFalse(DataTypeComparer.compare(intType, struct));
    }

    @Test
    void testEmptyStruct() {
        StructType emptyStruct1 = new StructType(Arrays.asList());
        StructType emptyStruct2 = new StructType(Arrays.asList());
        assertTrue(DataTypeComparer.compare(emptyStruct1, emptyStruct2));
    }

    @Test
    void testArrayOfDifferentVarCharLengths() {
        ArrayType<?> array1 = new ArrayType<>(varchar10, 2);
        ArrayType<?> array2 = new ArrayType<>(varchar20, 2);
        assertFalse(DataTypeComparer.compare(array1, array2));
    }

    @Test
    void testVarCharVsDifferentType() {
        assertFalse(DataTypeComparer.compare(varchar10, intType));
    }

    @Test
    void testBytesVsDifferentType() {
        assertFalse(DataTypeComparer.compare(bytes5, intType));
    }
}