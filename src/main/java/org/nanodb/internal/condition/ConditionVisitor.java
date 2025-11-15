package org.nanodb.internal.condition;

public interface ConditionVisitor {
    boolean visitComparison(ComparisonCondition comparisonCondition);
}
