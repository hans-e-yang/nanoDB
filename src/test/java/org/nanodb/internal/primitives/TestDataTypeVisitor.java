package org.nanodb.internal.primitives;

/**
 * Simple test visitor implementation returning a static string so tests can
 * verify the visitor pattern wiring without depending on complex logic.
 */
public class TestDataTypeVisitor implements DataTypeVisitor<String> {
    private static final String RESULT = "visited";

    @Override
    public <T2> String visit(ArrayType<T2> d) {
        return RESULT;
    }

    @Override
    public String visit(BooleanType d) {
        return RESULT;
    }

    @Override
    public String visit(BytesType d) {
        return RESULT;
    }

    @Override
    public String visit(DateType d) {
        return RESULT;
    }

    @Override
    public String visit(Int32Type d) {
        return RESULT;
    }

    @Override
    public String visit(StructType d) {
        return RESULT;
    }

    @Override
    public String visit(VarCharType d) {
        return RESULT;
    }
}
