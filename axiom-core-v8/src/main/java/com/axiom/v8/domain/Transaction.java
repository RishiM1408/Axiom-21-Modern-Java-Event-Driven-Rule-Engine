package com.axiom.v8.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Legacy Java 8 POJO implementation of Transaction.
 * boilerplate heavy.
 */
public class Transaction {
    private UUID id;
    private BigDecimal amount;
    private String accountId;
    private String merchantCategory;
    private Instant timestamp;

    public Transaction() {
    }

    public Transaction(UUID id, BigDecimal amount, String accountId, String merchantCategory, Instant timestamp) {
        this.id = id;
        this.amount = amount;
        this.accountId = accountId;
        this.merchantCategory = merchantCategory;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(String merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(merchantCategory, that.merchantCategory) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, accountId, merchantCategory, timestamp);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", accountId='" + accountId + '\'' +
                ", merchantCategory='" + merchantCategory + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
