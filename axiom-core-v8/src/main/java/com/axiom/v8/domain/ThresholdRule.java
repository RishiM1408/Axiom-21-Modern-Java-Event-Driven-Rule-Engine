package com.axiom.v8.domain;

import java.math.BigDecimal;

public class ThresholdRule extends Rule {
    private BigDecimal maxAmount;

    public ThresholdRule() {
    }

    public ThresholdRule(String ruleId, int priority, BigDecimal maxAmount) {
        super(ruleId, priority);
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
}
