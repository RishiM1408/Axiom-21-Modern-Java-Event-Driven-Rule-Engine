package com.axiom.v17.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Java 17: Sealed Classes (JEP 409).
 * We can now restrict the hierarchy.
 */
public sealed interface Rule permits ThresholdRule, LocationRule {
    String ruleId();

    int priority();
}

record ThresholdRule(
        String ruleId,
        int priority,
        BigDecimal maxAmount) implements Rule {
}

record LocationRule(
        String ruleId,
        int priority,
        List<String> allowedRegions) implements Rule {
}
