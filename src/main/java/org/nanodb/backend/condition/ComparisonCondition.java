package org.nanodb.backend.condition;

public class ComparisonCondition implements Condition {
    private String column;
    private ComparisonOperation op;
    private Object value;

    public String getColumn() {
        return column;
    }

    public ComparisonOperation getOp() {
        return op;
    }

    public Object getValue() {
        return value;
    }

    public ComparisonCondition(String column, ComparisonOperation op, String value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    /**
     * Sets the parsed value. Used by Executor to update the value after type parsing.
     * @param parsedValue The parsed object value according to schema
     */
    public void setParsedValue(Object parsedValue) {
        this.value = parsedValue;
    }

    /**
     * Gets the raw string value (before parsing)
     * @return The string value if not yet parsed, or the parsed value
     */
    public String getStringValue() {
        return this.value instanceof String ? (String) this.value : this.value.toString();
    }

    public boolean accept(ConditionVisitor visitor) {
        return visitor.visitComparison(this);
    }
}