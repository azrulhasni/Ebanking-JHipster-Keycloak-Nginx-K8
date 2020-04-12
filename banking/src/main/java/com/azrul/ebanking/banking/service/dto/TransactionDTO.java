package com.azrul.ebanking.banking.service.dto;

import java.time.ZonedDateTime;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import com.azrul.ebanking.banking.domain.enumeration.Currency;

/**
 * A DTO for the {@link com.azrul.ebanking.banking.domain.Transaction} entity.
 */
public class TransactionDTO implements Serializable {
    
    private Long id;

    private String transactionId;

    private String transactionType;

    private BigDecimal amount;

    private ZonedDateTime time;

    private Currency currency;


    private Long sourceId;

    private Long targetId;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long productAccountId) {
        this.sourceId = productAccountId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long productAccountId) {
        this.targetId = productAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransactionDTO transactionDTO = (TransactionDTO) o;
        if (transactionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), transactionDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
            "id=" + getId() +
            ", transactionId='" + getTransactionId() + "'" +
            ", transactionType='" + getTransactionType() + "'" +
            ", amount=" + getAmount() +
            ", time='" + getTime() + "'" +
            ", currency='" + getCurrency() + "'" +
            ", sourceId=" + getSourceId() +
            ", targetId=" + getTargetId() +
            "}";
    }
}
