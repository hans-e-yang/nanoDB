package org.nanodb.backend.primitives.visitor;

import org.nanodb.backend.primitives.*;
import org.nanodb.utils.Pair;

import java.util.List;

public class DataTypeComparer implements DataTypeVisitor<Boolean> {
    DataType<?> type1;
    DataType<?> type2;

    public DataTypeComparer(DataType<?> type1, DataType<?> type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    public static Boolean compare(DataType<?> type1, DataType<?> type2) {
        return (new DataTypeComparer(type1, type2)).compare();
    }

    private Boolean compare() {return this.type1.accept(this);}

    private boolean sameClass() {return type1.getClass().equals(type2.getClass());}

    @Override
    public Boolean visit(DateType d) {
        return sameClass();
    }

    @Override
    public <T2> Boolean visit(ArrayType<T2> d) {
        if (!sameClass()) return Boolean.FALSE;
        ArrayType<?> d2 = (ArrayType<?>) type2;
        if (d.getLength() != d2.getLength()) return Boolean.FALSE;

        type1 = d.getElementType();
        type2 = d2.getElementType();
        return type1.accept(this);
    }

    @Override
    public Boolean visit(Int32Type d) {
        return sameClass();
    }

    @Override
    public Boolean visit(VarCharType d) {
        if (!sameClass()) return Boolean.FALSE;
        VarCharType d2 = (VarCharType) type2;
        return d.getLength() != d2.getLength();
    }

    @Override
    public Boolean visit(StructType d) {
        if (!sameClass()) return Boolean.FALSE;
        StructType d2 = (StructType) type2;

        List<Pair<String, DataType<?>>> fields = d.getFields();
        List<Pair<String, DataType<?>>> fields2 = d2.getFields();
        if (fields.size() != fields2.size()) return Boolean.FALSE;

        for (int i = 0; i < fields.size(); i++) {
            Pair<String, DataType<?>> pair1 = fields.get(i);
            Pair<String, DataType<?>> pair2 = fields2.get(i);

            if (!pair1.getKey().equals(pair2.getKey())) return Boolean.FALSE;

            type1 = pair1.getValue();
            type2 = pair2.getValue();

            if (!type1.accept(this)) return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
