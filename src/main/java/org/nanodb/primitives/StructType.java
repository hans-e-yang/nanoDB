package org.nanodb.primitives;

import org.nanodb.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StructType implements DataType<Map<String, Object>> {
    private final List<Pair<String, DataType<?>>> fields;

    /**
     * Constructs a StructType with the specified fields.
     *
     * @param fields a list of pairs containing field names and their corresponding DataTypes
     */
    public StructType(List<Pair<String, DataType<?>>> fields) {
        this.fields = fields;
    }

    /**
     * Converts a byte array into a Map representing the structured data.
     *
     * @param bytes  the byte array containing the data
     * @param offset the starting position in the byte array
     * @return a Map containing the field names and their corresponding values
     */
    @Override
    public Map<String, Object> fromBytes(byte[] bytes, int offset) {
        Map<String, Object> row = new HashMap<>();
        int currentOffset = offset;

        // Recursively call fromBytes into all fields
        for (Pair<String, DataType<?>> field : fields) {
            String fieldName = field.getKey();
            DataType<?> dataType = field.getValue();
            Object value = dataType.fromBytes(bytes, currentOffset);
            row.put(fieldName, value);
            currentOffset += dataType.byteSize();
        }

        return row;
    }

    /**
     * Converts a Map representing structured data into a byte array.
     *
     * @param data the Map containing field names and their corresponding values
     * @return a byte array representing the structured data
     */
    public byte[] toBytes(Map<String, Object> data) {
        byte[] result = new byte[byteSize()]; // Allocate byte array based on total size
        int currentOffset = 0;

        // Iterate through each field and convert the value to bytes
        for (Pair<String, DataType<?>> field : fields) {
            String fieldName = field.getKey();
            DataType dataType = field.getValue();
            Object value = data.get(fieldName);

            try {
                // Convert the value to bytes and copy it into the result array
                byte[] bytes = dataType.toBytes(value);
                System.arraycopy(bytes, 0, result, currentOffset, bytes.length);
                currentOffset += bytes.length;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Invalid value type for field " +
                                fieldName +
                                ". Expected DataType: " +
                                dataType.getClass() +
                                ". Actual type: " +
                                value.getClass()
                );
            }
        }

        return result;
    }

    /**
     * Calculates the total byte size required for the structured data.
     *
     * @return the total byte size of all fields combined
     */
    @Override
    public int byteSize() {
        int size = 0;
        for (Pair<String, DataType<?>> field : fields) {
            size += field.getValue().byteSize();
        }
        return size;
    }
}
