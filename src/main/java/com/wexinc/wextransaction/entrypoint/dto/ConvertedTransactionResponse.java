package com.wexinc.wextransaction.entrypoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConvertedTransactionResponse(
        Long id,
        String description,
        LocalDate transactionDate,
        BigDecimal amount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {}