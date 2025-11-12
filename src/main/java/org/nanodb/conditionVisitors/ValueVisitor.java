package org.nanodb.conditionVisitors;

import org.nanodb.condition.ComparisonCondition;
import org.nanodb.condition.ComparisonOperation;
import org.nanodb.condition.ConditionVisitor;

import java.util.List;
import java.util.Map;

// This will compare the direct values with the condition itself
// In the future, we may add another visitor to handle indexes
public class ValueVisitor implements ConditionVisitor {
    Map<String, Object> struct;
    public ValueVisitor(Map<String, Object> struct) {
        this.struct = struct;
    }

    private Object GetValue(String path) throws InvalidColumnNameException {
        String[] fields = path.split("\\.");
        Object current = struct;

        for (String field : fields) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(field);
                if (current == null) {
                    throw new InvalidColumnNameException("Column not found: " + field);
                }
            } else if (current instanceof List) {
                try {
                    int index = Integer.parseInt(field); // Convert field to an integer index
                    current = ((List<?>) current).get(index);
                } catch (NumberFormatException e) {
                    throw new InvalidColumnNameException("Invalid index: " + field);
                } catch (IndexOutOfBoundsException e) {
                    throw new InvalidColumnNameException("Index out of bounds: " + field);
                }
            }
            else {
                throw new InvalidColumnNameException("Invalid path: " + path);
            }
        }

        return current;
    }

    private int compareValues(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        throw new IllegalArgumentException("Cannot compare values: " + a + " and " + b);
    }

    @Override
    public boolean visitComparison(ComparisonCondition comparisonCondition) {
        // Check if the comparisons are first valid.
        ComparisonOperation op = comparisonCondition.getOp();

        String path = comparisonCondition.getColumn();
        Object expectedValue = comparisonCondition.getValue();
        Object actualValue = GetValue(path);

        // Validate the types
        Class<?> expectedType = expectedValue.getClass();
        Class<?> actualType = expectedValue.getClass();

        throw new InvalidColumnNameException("Incomplete column path. Column path should point to a value");


    }

}
