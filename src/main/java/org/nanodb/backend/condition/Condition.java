package org.nanodb.backend.condition;

public interface Condition {
    public boolean accept(ConditionVisitor visitor);
}