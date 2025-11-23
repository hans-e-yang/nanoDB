package org.nanodb.internal.condition;

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

    public ComparisonCondition(String column, ComparisonOperation op, Object value) {
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

    public boolean accept(ConditionVisitor visitor) {
        return visitor.visitComparison(this);
    }
}