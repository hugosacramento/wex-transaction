package com.wexinc.wextransaction.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction (
    Long id,
    String description,
    LocalDate transactionDate,
    BigDecimal amount
) {

    public Transaction withAmount(BigDecimal newAmount) {
        return new Transaction(
                this.id,
                this.description,
                this.transactionDate,
                newAmount
        );
    }
}