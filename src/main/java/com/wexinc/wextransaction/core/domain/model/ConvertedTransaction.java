package com.wexinc.wextransaction.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConvertedTransaction(
        Long id,
        String description,
        LocalDate transactionDate,
        BigDecimal amount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {}