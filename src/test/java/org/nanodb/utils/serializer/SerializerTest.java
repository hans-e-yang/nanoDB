package org.nanodb.utils.serializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SerializerTest<DeserializedType, SerializedType> {
    protected Serializer<DeserializedType, SerializedType> serializer;

    protected abstract Serializer<DeserializedType, SerializedType> createSerializer();

    @BeforeEach
    public void setUp() {
        this.serializer = createSerializer();
    }

    // Any teardown necessary for individual implementations
    @AfterEach
    public void tearDown() {}

    // Helper functions to test the serialization and deserialization process
    protected void testSerializedProperly(DeserializedType d) {
        assertEquals(serializer.deserialize(serializer.serialize(d)), d);
    }

    protected void testDeserializedProperly(SerializedType d) {
        assertEquals(serializer.serialize(serializer.deserialize(d)), d);
    }
}
