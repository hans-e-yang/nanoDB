package org.nanodb.internal.condition;

public class BooleanCondition implements Condition {
    boolean value;
    private BooleanCondition(boolean val) {
        this.value = val;
    }

    public static final BooleanCondition TRUE = new BooleanCondition(true);
    public static final BooleanCondition FALSE = new BooleanCondition(false);

    @Override
    public boolean accept(ConditionVisitor visitor) {
        return value;
    }
}
