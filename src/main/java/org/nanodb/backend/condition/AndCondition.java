package org.nanodb.backend.condition;

public class AndCondition implements Condition {
    private Condition left;
    private Condition right;

    public AndCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    public Condition getLeft() {
        return left;
    }

    public Condition getRight() {
        return right;
    }

    public boolean accept(ConditionVisitor visitor) {
        return this.left.accept(visitor) && this.right.accept(visitor);
    }
}