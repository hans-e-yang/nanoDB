package org.nanodb.cli.statement.condition;

import org.nanodb.internal.db.Row;

public abstract class Condition {
    public abstract boolean evaluate(Row row);
}