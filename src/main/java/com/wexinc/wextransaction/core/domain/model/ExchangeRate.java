package com.wexinc.wextransaction.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(
    String countryCurrencyDesc,
    BigDecimal exchangeRate,
    LocalDate effectiveDate
) {}