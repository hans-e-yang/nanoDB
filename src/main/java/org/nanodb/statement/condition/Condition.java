package org.nanodb.statement.condition;

import org.nanodb.db.Row;

public abstract class Condition {
    public abstract boolean evaluate(Row row);
}