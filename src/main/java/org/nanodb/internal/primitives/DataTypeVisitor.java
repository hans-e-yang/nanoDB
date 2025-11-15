package org.nanodb.internal.primitives;

public interface DataTypeVisitor<T> {
    <T2> T visit(ArrayType<T2> d);
    T visit(BooleanType d);
    T visit(BytesType d);
    T visit(DateType d);
    T visit(Int32Type d);
    T visit(StructType d);
    T visit(VarCharType d);
}
