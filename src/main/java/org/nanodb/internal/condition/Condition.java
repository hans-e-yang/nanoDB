package org.nanodb.internal.condition;

public interface Condition {
    public boolean accept(ConditionVisitor visitor);
}