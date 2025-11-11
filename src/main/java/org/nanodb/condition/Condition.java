package org.nanodb.condition;

public interface Condition {
    public boolean accept(ConditionVisitor visitor);
}