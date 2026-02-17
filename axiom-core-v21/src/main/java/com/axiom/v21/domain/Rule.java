package com.axiom.v21.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * A sealed hierarchy representing the various types of business logic rules.
 * <p>
 * Demonstrates Java 17 Sealed Classes/Interfaces (JEP 409).
 * - Restricts which classes can implement this interface.
 * - Enables exhaustive pattern matching in switch expressions (JEP 441) in Java
 * 21.
 * </p>
 */
public sealed interface Rule permits ThresholdRule, LocationRule, FrequencyRule {
    /**
     * @return The unique identifier of the rule.
     */
    String ruleId();

    /**
     * @return The priority of the rule (higher executes execution order logic if
     *         needed).
     */
    int priority();
}

/**
 * A rule that checks if a transaction amount exceeds a maximum limit.
 * <p>
 * Demonstrates a Record implementing a Sealed Interface.
 * </p>
 *
 * @param ruleId    Unique Rule ID.
 * @param priority  Rule priority.
 * @param maxAmount The maximum allowed amount for a transaction.
 */
record ThresholdRule(
        String ruleId,
        int priority,
        BigDecimal maxAmount) implements Rule {
}

/**
 * A rule that checks if a transaction occurred in an allowed region (simulated
 * by merchant category for now,
 * or we could add a region field to Transaction).
 *
 * @param ruleId         Unique Rule ID.
 * @param priority       Rule priority.
 * @param allowedRegions List of allowed region codes (e.g., "US", "EU").
 */
record LocationRule(
        String ruleId,
        int priority,
        List<String> allowedRegions) implements Rule {
}

/**
 * A dummy rule to demonstrate exhaustiveness (e.g., checking transaction
 * frequency).
 * For this demo, it might just check a flag or always pass.
 *
 * @param ruleId            Unique Rule ID.
 * @param priority          Rule priority.
 * @param timeWindowSeconds The window to check frequency (placeholder).
 */
record FrequencyRule(
        String ruleId,
        int priority,
        long timeWindowSeconds) implements Rule {
}
