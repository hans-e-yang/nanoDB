package org.nanodb.condition;

import org.nanodb.db.Row;

public interface Condition {
    public boolean accept(ConditionVisitor visitor);
}