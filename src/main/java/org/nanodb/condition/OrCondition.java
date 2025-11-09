package org.nanodb.condition;

import org.nanodb.db.Row;

public class OrCondition implements Condition {
    private Condition left;
    private Condition right;

    public OrCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    public boolean accept(ConditionVisitor visitor) {
        return this.left.accept(visitor) || this.right.accept(visitor);
    }

}