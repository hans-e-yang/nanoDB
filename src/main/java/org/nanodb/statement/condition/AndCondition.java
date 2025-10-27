package org.nanodb.statement.condition;

import org.nanodb.db.Row;

public class AndCondition extends Condition {
    private Condition left;
    private Condition right;

    public AndCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean evaluate(Row row) {
        return left.evaluate(row) && right.evaluate(row);
    }
}