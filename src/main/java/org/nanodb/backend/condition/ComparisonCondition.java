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

    public boolean accept(ConditionVisitor visitor) {
        return visitor.visitComparison(this);
    }
}