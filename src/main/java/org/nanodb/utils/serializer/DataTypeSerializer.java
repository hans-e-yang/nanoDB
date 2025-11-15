package org.nanodb.utils.serializer;

import java.util.ArrayList;
import java.util.List;

import org.nanodb.internal.primitives.*;
import org.nanodb.utils.Reader;
import org.nanodb.utils.Writer;
import org.nanodb.utils.Pair;

/**
 * Serializes and Deserializes a DataType into a byte array representation.
 */
public class DataTypeSerializer implements Serializer<byte[], DataType<?>> {
    // Lazy singleton
    private static DataTypeSerializer _instance = null;
    public static DataTypeSerializer getInstance() {
        if (_instance == null) _instance = new DataTypeSerializer();
        return _instance;
    }
    private DataTypeSerializer(){}

    // Enum to differentiate between different data types
    private enum DataTypeEnum {
        DateType("d"),
        VarCharType("v"),
        Int32Type("i"),
        StructType("s"),
        ArrayType("a"),
        BooleanType("B"),
        BytesType("b");

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
                if (error.getId().equals(description)) {
                    return error;
                }
            }
            throw new IllegalArgumentException("No enum constant for description: " + description);
        }
    }

    // Useful types to keep forever
    private static final VarCharType charType = new VarCharType(1);
    private static final Int32Type int32Type = new Int32Type();
    private static final VarCharType columnNameType = new VarCharType(64);


    private static class Deserializer implements DataTypeVisitor<byte[]> {

        private static Writer writeType(DataTypeEnum type) {
            Writer w = new Writer();
            w.write(charType, type.getId());
            return w;
        }

        public <T> byte[] visit(ArrayType<T> d) {
            Writer w = writeType(DataTypeEnum.ArrayType);

            // Insert size of the array
            w.write(int32Type, d.getLength());
            // Insert type of the elements
            w.write(d.getElementType().accept(this));

            return w.array();
        }

        public byte[] visit(Int32Type d) {
            return writeType(DataTypeEnum.Int32Type).array();
        }

        public byte[] visit(BooleanType d) {
            return writeType(DataTypeEnum.BooleanType).array();
        }

        public byte[] visit(DateType d) {
            return writeType(DataTypeEnum.DateType).array();
        }

        public byte[] visit(VarCharType d) {
            Writer w = writeType(DataTypeEnum.VarCharType);
            // Insert max string length
            w.write(int32Type, d.getLength());
            return w.array();
        }

        public byte[] visit(BytesType d) {
            Writer w = writeType(DataTypeEnum.BytesType);
            // Insert max string length
            w.write(int32Type, d.getLength());
            return w.array();
        }

        public byte[] visit(StructType d) {
            Writer w = writeType(DataTypeEnum.StructType);
            // Insert column data count
            w.write(int32Type, d.getFields().size());
            // Insert all the field and records in order
            for (Pair<String, DataType<?>> field : d.getFields()) {
                w.write(columnNameType, field.getKey());
                w.write(field.getValue().accept(this));
            }
            return w.array();
        }
    }

    // Variable length parser/serializer
    private static class Serializer {
        private final Reader reader;
        public Serializer(byte[] data) {
            this.reader = new Reader(data);
        }
        public Serializer(byte[] data, int offset) {
            this.reader = new Reader(data, offset);
        }

        public DataType<?> parse() {
            DataTypeEnum type = DataTypeEnum.fromString(
                    reader.read(charType)
            );

            // Must support all possible types
            return switch (type) {
                case DateType -> parseDateType();
                case ArrayType -> parseArrayType();
                case Int32Type -> parseInt32Type();
                case StructType -> parseStructType();
                case VarCharType -> parseVarCharType();
                case BytesType -> parseBytesType();
                case BooleanType -> parseBooleanType();
            };
        }

        public DateType parseDateType() {
            return new DateType();
        }

        public ArrayType<?> parseArrayType() {
            Integer length = reader.read(int32Type);
            DataType<?> type = parse();
            return new ArrayType<>(type, length);
        }

        public Int32Type parseInt32Type() {
            return new Int32Type();
        }

        public StructType parseStructType() {
            Integer length = reader.read(int32Type);
            List<Pair<String, DataType<?>>> fields = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                fields.add(new Pair<>(
                        reader.read(columnNameType),
                        parse()
                ));
            }
            return new StructType(fields);
        }

        public VarCharType parseVarCharType() {
            Integer length = reader.read(int32Type);
            return new VarCharType(length);
        }

        public BooleanType parseBooleanType() {
            return new BooleanType();
        }

        public BytesType parseBytesType() {
            Integer length = reader.read(int32Type);
            return new BytesType(length);
        }
    }

    private static final Deserializer deserializer = new Deserializer();
    /**
     * Deserializes a datatype into its byte representation,
     * Allowing storage of the datatype itself (Metadata)
     */
    @Override
    public byte[] deserialize(DataType<?> type) {
        return type.accept(deserializer);
    }

    @Override
    public DataType<?> serialize(byte[] value) {
        return (new Serializer(value)).parse();
    }

    public DataType<?> serialize(byte[] value, int offset) {
        return (new Serializer(value, offset)).parse();
    }
}
