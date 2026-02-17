package com.axiom.v21.domain;

import java.util.UUID;

/**
 * Represents the result of evaluating a Rule against a Transaction.
 * <p>
 * This record captures the outcome, including the pass/fail status and the
 * reason using
 * Java 14+ Records.
 * </p>
 *
 * @param transactionId The ID of the transaction evaluated.
 * @param ruleId        The ID of the rule applied.
 * @param passed        True if the transaction passed the rule, false
 *                      otherwise.
 * @param reason        A descriptive reason for the result.
 */
public record EvaluationResult(
        UUID transactionId,
        String ruleId,
        boolean passed,
        String reason) {
}
