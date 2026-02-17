package com.axiom.v21.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an immutable financial transaction event.
 * <p>
 * Demonstrates Java 14+ Records (JEP 395).
 * - Concise syntax (no boilerplate getters/constructors).
 * - Immutable by default.
 * - Built-in equals/hashCode/toString.
 * </p>
 *
 * @param id               Unique transaction identifier.
 * @param amount           The transaction amount.
 * @param accountId        The account ID initiating the transaction.
 * @param merchantCategory The category of the merchant (e.g., "ELECTRONICS",
 *                         "GROCERY").
 * @param timestamp        When the transaction occurred.
 */
public record Transaction(
        UUID id,
        BigDecimal amount,
        String accountId,
        String merchantCategory,
        Instant timestamp) {
}
