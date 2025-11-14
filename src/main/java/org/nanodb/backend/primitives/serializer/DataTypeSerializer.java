package org.nanodb.backend.primitives.serializer;

import java.util.ArrayList;
import java.util.List;

import org.nanodb.backend.primitives.ArrayType;
import org.nanodb.backend.primitives.DataType;
import org.nanodb.backend.primitives.DataTypeVisitor;
import org.nanodb.backend.primitives.DateType;
import org.nanodb.backend.primitives.Int32Type;
import org.nanodb.backend.primitives.StructType;
import org.nanodb.backend.primitives.VarCharType;
import org.nanodb.utils.ByteExtensions;
import org.nanodb.utils.Pair;

/**
 * Serializes and Deserializes a DataType into a byte array representation.
 */
public class DataTypeSerializer implements Serializer<byte[], Object> {
    private enum DataTypeEnum {
        DateType("d"),
        VarCharType("v"),
        Int32Type("i"),
        StructType("s"),
        ArrayType("a");

        private final String id;

        DataTypeEnum(String description) {
            this.id = description;
        }

        public String getId() {
            return id;
        }

        // Static method to get enum from string
        public static DataTypeEnum fromString(String description) {
            for (DataTypeEnum error : DataTypeEnum.values()) {
                if (error.getId().equalsIgnoreCase(description)) {
                    return error;
                }
            }
            throw new IllegalArgumentException("No enum constant for description: " + description);
        }
    }

    private static VarCharType charType = new VarCharType(1);
    private static Int32Type int32Type = new Int32Type();
    private static VarCharType columnNameType = new VarCharType(64);

    private static class Deserializer implements DataTypeVisitor<byte[]> {
        public byte[] visit(DateType d) {
            List<byte[]> data = new ArrayList<>();
            // Insert data type
            data.add(charType.toBytes(DataTypeEnum.DateType.getId()));
            return ByteExtensions.concat(data);
        }

        public <T> byte[] visit(ArrayType<T> d) {
            List<byte[]> data = new ArrayList<>();

            // Insert data type
            data.add(charType.toBytes(DataTypeEnum.ArrayType.getId()));
            // Insert size of the array
            data.add(int32Type.toBytes(d.getLength()));
            // Insert type of the elements
            data.add(d.getElementType().accept(this));

            return ByteExtensions.concat(data);
        }

        public byte[] visit(Int32Type d) {
            List<byte[]> data = new ArrayList<>();
            // Insert data type
            data.add(charType.toBytes(DataTypeEnum.Int32Type.getId()));
            return ByteExtensions.concat(data);
        }

        public byte[] visit(VarCharType d) {
            List<byte[]> data = new ArrayList<>();
            // Insert data type
            data.add(charType.toBytes(DataTypeEnum.VarCharType.getId()));
            // Insert max string length
            data.add(int32Type.toBytes(d.getLength()));
            return ByteExtensions.concat(data);
        }

        public byte[] visit(StructType d) {
            List<byte[]> data = new ArrayList<>();
            // Insert data type
            data.add(charType.toBytes(DataTypeEnum.StructType.getId()));
            // Insert all the field and records in order
            for (Pair<String, DataType<Object>> field : d.getFields()) {
                data.add(columnNameType.toBytes(field.getKey()));
                data.add(field.getValue().accept(this));
            }
            return ByteExtensions.concat(data);
        }
    }

    // Variable length parser/serializer
    private class Serializer {
        private byte[] data;
        private int offset = 0;
        public Serializer(byte[] data) {
            this.data = data;
        }
        public DataType<Object> parse() {
            DataTypeEnum type = DataTypeEnum.fromString(
                charType.fromBytes(data, this.offset)
            );
            this.offset += charType.byteSize();
            DataType<Object> d;
            d = switch (type) {
                case DateType ->
                case ArrayType ->
                case Int32Type ->
                case StructType ->
                case VarCharType ->
                default -> null;
            }

            return d;
        }

        public DateType parseDateType() {

        }

        public ArrayType<Object> parseDateType() {

        }

        public Int32Type parseDateType() {

        }

        public StructType parseDateType() {

        }

        public VarCharType parseDateType() {

        }
    }

    private static Deserializer deserializer = new Deserializer();

    /**
     * Deserializes a datatype into its byte representation,
     * Allowing storage of the datatype itself (Metadata)
     */
    public byte[] deserialize(DataType<Object> type) {
        return type.accept(deserializer);
    }

    /**
     * @param value - Ignored, this function will only run on the type
     */
    @Override
    public byte[] deserialize(DataType<Object> type, Object value) {
        return deserialize(type);
    }

    @Override
    public DataType<Object> serialize(DataType<Object> type, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }
}
