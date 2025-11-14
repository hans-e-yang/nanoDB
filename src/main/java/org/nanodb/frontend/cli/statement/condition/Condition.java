package org.nanodb.frontend.cli.statement.condition;

import org.nanodb.backend.db.Row;

public abstract class Condition {
    public abstract boolean evaluate(Row row);
}