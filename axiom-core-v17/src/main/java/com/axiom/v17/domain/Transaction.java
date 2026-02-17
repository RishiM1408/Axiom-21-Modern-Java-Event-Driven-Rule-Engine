package com.axiom.v17.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Java 17: We finally have Records! (JEP 395)
 * No more boilerplate.
 */
public record Transaction(
        UUID id,
        BigDecimal amount,
        String accountId,
        String merchantCategory,
        Instant timestamp) {
}
