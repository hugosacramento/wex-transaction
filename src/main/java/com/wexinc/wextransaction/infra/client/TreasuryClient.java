package com.wexinc.wextransaction.infra.client;

import com.wexinc.wextransaction.core.domain.model.ExchangeRate;
import com.wexinc.wextransaction.core.gateway.TreasuryGateway;
import com.wexinc.wextransaction.infra.client.dto.TreasuryRatesResponse;
import com.wexinc.wextransaction.infra.client.feign.TreasuryFeign;
import com.wexinc.wextransaction.infra.client.mapper.ExchangeDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreasuryClient implements TreasuryGateway {

    private final TreasuryFeign treasuryFeign;
    private final ExchangeDtoMapper mapper;

    public Optional<ExchangeRate> findLatestRate(String countryCurrencyDesc, LocalDate date) {
        log.info("Fetching exchange rate for {} on {}", countryCurrencyDesc, date);
        String filter = "country_currency_desc:eq:" + countryCurrencyDesc + ",effective_date:lte:" + date;
        String fields = "effective_date,exchange_rate,country_currency_desc";
        String sort = "-effective_date";
        Integer pageSize = 1;

        log.info("Parameters for TreasureRate call: filter '{}', fields '{}', sort '{}', pageSize '{}'",
                filter, fields, sort, pageSize);
        TreasuryRatesResponse treasuryRatesResponse = treasuryFeign.getRatesOfExchange(filter, fields, sort, pageSize);

        if (treasuryRatesResponse == null ||
                treasuryRatesResponse.getData() == null ||
                treasuryRatesResponse.getData().isEmpty()) {
            log.info("Nothing Found for Exchange rate for {} on {}.", countryCurrencyDesc, date);
            return Optional.empty();
        }

        log.info("Result for Exchange rate for {} on {}. {} rows. {}",
                countryCurrencyDesc, date, treasuryRatesResponse.getData().size(), treasuryRatesResponse);

        return Optional.of(mapper.toDomain(treasuryRatesResponse.getData().getFirst()));
    }
}
