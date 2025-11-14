package org.nanodb.backend.condition;

public interface ConditionVisitor {
    boolean visitComparison(ComparisonCondition comparisonCondition);
}
