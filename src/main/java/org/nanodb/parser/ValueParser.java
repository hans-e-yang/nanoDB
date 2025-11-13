package org.nanodb.parser;

import org.nanodb.condition.*;
import org.nanodb.primitives.*;
import org.nanodb.utils.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * ValueParser handles parsing of string values to their appropriate types
 * according to the schema definition.
 * 
 * This class centralizes all type parsing logic, making it reusable and testable.
 */
public class ValueParser {

    /**
     * Parse a map of string values according to the schema types
     * 
     * @param stringValues Map of column names to string values
     * @param schema The StructType schema defining expected types
     * @return Map of column names to parsed Object values
     * @throws ParseException if parsing fails
     */
    public static Map<String, Object> parseValues(Map<String, String> stringValues, StructType schema) throws ParseException {
        if (schema == null) {
            throw new ParseException("Schema is null", 0);
        }
        
        Map<String, Object> parsedValues = new HashMap<>();
        
        for (Map.Entry<String, String> entry : stringValues.entrySet()) {
            String columnName = entry.getKey();
            String stringValue = entry.getValue();
            
            // Find the column type in schema
            DataType<?> expectedType = getColumnType(columnName, schema);
            
            if (expectedType == null) {
                throw new ParseException("Column not found in schema: " + columnName, 0);
            }
            
            // Parse the string value to the appropriate type
            Object parsedValue = parseValue(stringValue, expectedType);
            parsedValues.put(columnName, parsedValue);
        }
        
        return parsedValues;
    }

    /**
     * Parse a single string value to the appropriate type based on DataType
     * 
     * @param value The string value to parse
     * @param dataType The target data type
     * @return The parsed object value
     * @throws ParseException if parsing fails
     */
    public static Object parseValue(String value, DataType<?> dataType) throws ParseException {
        if (value == null) {
            return null;
        }
        
        value = value.trim();
        
        if (dataType instanceof Int32Type) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ParseException("Cannot parse '" + value + "' as integer", 0);
            }
        } else if (dataType instanceof VarCharType) {
            return value; // Already a string
        } else if (dataType instanceof DateType) {
            return parseDate(value);
        } else {
            throw new ParseException("Unsupported data type: " + dataType.getClass().getName(), 0);
        }
    }

    /**
     * Parse condition values according to schema.
     * This method recursively traverses the condition tree and parses all comparison values.
     * Returns a new condition tree with parsed values.
     * 
     * @param condition The condition to parse (can be ComparisonCondition, AndCondition, or OrCondition)
     * @param schema The StructType schema defining expected types
     * @return A new condition tree with parsed values, or null if input is null
     * @throws ParseException if parsing fails or column not found in schema
     */
    public static Condition parseConditionValues(Condition condition, StructType schema) throws ParseException {
        if (condition == null) {
            return null;
        }

        if (condition instanceof ComparisonCondition) {
            // Base case: parse the comparison value and create a new condition
            ComparisonCondition comp = (ComparisonCondition) condition;
            String columnName = comp.getColumn();
            String stringValue = comp.getStringValue();

            // Find the column type in schema
            DataType<?> expectedType = getColumnType(columnName, schema);

            if (expectedType == null) {
                throw new ParseException("Column not found in schema: " + columnName, 0);
            }

            // Parse the string value to the appropriate type
            Object parsedValue = parseValue(stringValue, expectedType);
            
            // Create a new ComparisonCondition with the parsed value
            ComparisonCondition parsedCondition = new ComparisonCondition(columnName, comp.getOp(), stringValue);
            parsedCondition.setParsedValue(parsedValue);
            return parsedCondition;

        } else if (condition instanceof AndCondition) {
            // Recursive case: traverse left and right children, create new AndCondition
            AndCondition andCond = (AndCondition) condition;
            Condition parsedLeft = parseConditionValues(andCond.getLeft(), schema);
            Condition parsedRight = parseConditionValues(andCond.getRight(), schema);
            return new AndCondition(parsedLeft, parsedRight);

        } else if (condition instanceof OrCondition) {
            // Recursive case: traverse left and right children, create new OrCondition
            OrCondition orCond = (OrCondition) condition;
            Condition parsedLeft = parseConditionValues(orCond.getLeft(), schema);
            Condition parsedRight = parseConditionValues(orCond.getRight(), schema);
            return new OrCondition(parsedLeft, parsedRight);
        }
        
        return condition;
    }

    /**
     * Find the data type for a given column name in the schema
     * 
     * @param columnName The column name to look up
     * @param schema The schema to search in
     * @return The DataType for the column, or null if not found
     */
    private static DataType<?> getColumnType(String columnName, StructType schema) {
        for (Pair<String, DataType<?>> field : schema.getFields()) {
            if (field.getKey().equals(columnName)) {
                return field.getValue();
            }
        }
        return null;
    }

    /**
     * Parse a string value as a date, trying multiple date formats
     * 
     * @param value The string value to parse
     * @return The parsed Date object
     * @throws ParseException if the value cannot be parsed as a date
     */
    private static Object parseDate(String value) throws ParseException {
        // Try multiple date formats
        SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy/MM/dd"),
            new SimpleDateFormat("dd-MM-yyyy"),
            new SimpleDateFormat("dd/MM/yyyy")
        };
        
        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(value);
            } catch (ParseException e) {
                // Try next format
            }
        }
        throw new ParseException("Cannot parse '" + value + "' as date. Expected formats: yyyy-MM-dd, yyyy/MM/dd, dd-MM-yyyy, or dd/MM/yyyy", 0);
    }
}
