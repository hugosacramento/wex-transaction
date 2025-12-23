package com.wexinc.wextransaction.entrypoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(

        @NotBlank(message = "Description must not be blank")
        @Size(max = 50, message = "Description must not exceed 50 characters")
        String description,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @NotNull(message = "Purchase amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Purchase amount must be positive")
        BigDecimal amount
) {}