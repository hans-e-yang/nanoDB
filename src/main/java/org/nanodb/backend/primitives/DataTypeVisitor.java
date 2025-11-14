package org.nanodb.backend.primitives;

public interface DataTypeVisitor<T> {
    T visit(DateType d);
    <T2> T visit(ArrayType<T2> d);
    T visit(Int32Type d);
    T visit(VarCharType d);
    T visit(StructType d);
}
