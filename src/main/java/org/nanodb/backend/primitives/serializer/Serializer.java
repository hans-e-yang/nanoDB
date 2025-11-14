package org.nanodb.backend.primitives.serializer;

import org.nanodb.backend.primitives.DataType;

public interface Serializer<DeserializedType, SerializedType> {
    DeserializedType deserialize(SerializedType value);
    SerializedType serialize(DeserializedType value);
}
