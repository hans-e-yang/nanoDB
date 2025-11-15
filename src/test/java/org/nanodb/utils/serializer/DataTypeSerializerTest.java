package org.nanodb.utils.serializer;

import org.junit.jupiter.api.Test;
import org.nanodb.internal.primitives.*;
import org.nanodb.internal.primitives.visitor.DataTypeComparer;
import org.nanodb.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataTypeSerializerTest extends SerializerTest<byte[], DataType<?>> {
    @Override
    protected Serializer<byte[], DataType<?>> createSerializer() {
        return DataTypeSerializer.getInstance();
    }


    // Specifically for this test, we need to define our own testDeserializedProperly
    protected <T> DataType<T> testDeserializedProperly(DataType<T> d) {
        DataType<T> serialized = (DataType<T>) serializer.serialize(serializer.deserialize(d));
        assertTrue(DataTypeComparer.compare(
                d,
                serialized
        ));
        return d;
    }

    @Test
    public void test_date(){
        testDeserializedProperly(new DateType());
    }
    @Test
    public void test_int(){
        testDeserializedProperly(new Int32Type());
    }
    @Test
    public void test_string(){
        testDeserializedProperly(new VarCharType(5));
    }
    @Test
    public void test_array(){
        testDeserializedProperly(new ArrayType<>(new Int32Type(), 10));
    }
    @Test
    public void test_struct(){
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();
        fields.add(new Pair<>("col 1", new Int32Type()));
        fields.add(new Pair<>("col 2", new VarCharType(5)));
        fields.add(new Pair<>("col 3", new ArrayType<>(new Int32Type(), 10)));
        testDeserializedProperly(new StructType(fields));
    }
    @Test
    public void test_bytes(){
        testDeserializedProperly(new BytesType(1000));
    }
}
