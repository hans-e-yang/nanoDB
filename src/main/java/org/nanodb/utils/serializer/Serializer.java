package org.nanodb.utils.serializer;

public interface Serializer<DeserializedType, SerializedType> {
    DeserializedType deserialize(SerializedType value);
    SerializedType serialize(DeserializedType value);
}
