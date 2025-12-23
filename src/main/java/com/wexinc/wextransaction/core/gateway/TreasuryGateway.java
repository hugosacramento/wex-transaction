package com.wexinc.wextransaction.core.gateway;

import com.wexinc.wextransaction.core.domain.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;

public interface TreasuryGateway {
    Optional<ExchangeRate> findLatestRate(String countryCurrencyDesc, LocalDate date);
}
