package com.axiom.v8.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Legacy abstract base class for Rules.
 * No sealed hierarchy, open for extension.
 */
public abstract class Rule {
    protected String ruleId;
    protected int priority;

    public Rule() {
    }

    public Rule(String ruleId, int priority) {
        this.ruleId = ruleId;
        this.priority = priority;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
