package org.nanodb.backend.primitives.serializer;

import org.nanodb.backend.primitives.DataType;

public interface Serializer<DeserializedType, SerializedType> {
    DeserializedType deserialize(DataType<SerializedType> type, SerializedType value);
    SerializedType serialize(DataType<SerializedType> type, DeserializedType value);
}
