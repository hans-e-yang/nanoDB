package org.nanodb.condition;

public interface ConditionVisitor {
    boolean visitComparison(ComparisonCondition comparisonCondition);
}
