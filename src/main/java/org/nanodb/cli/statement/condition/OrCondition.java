package org.nanodb.cli.statement.condition;

import org.nanodb.internal.db.Row;

public class OrCondition extends Condition {
    private Condition left;
    private Condition right;

    public OrCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean evaluate(Row row) {
        return left.evaluate(row) || right.evaluate(row);
    }
}