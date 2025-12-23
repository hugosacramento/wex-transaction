package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;
import com.wexinc.wextransaction.core.domain.model.ExchangeRate;
import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.exception.ExchangeRateNotFoundException;
import com.wexinc.wextransaction.core.exception.TransactionNotFoundException;
import com.wexinc.wextransaction.core.gateway.TransactionGateway;
import com.wexinc.wextransaction.core.gateway.TreasuryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchPurchaseUsecaseImpl implements SearchPurchaseUsecase {

    private final TransactionGateway transactionGateway;
    private final TreasuryGateway treasuryGateway;

    private static final Period LOOKBACK = Period.ofMonths(6);

    @Override
    public ConvertedTransaction execute(Long id, String countryCurrency) {
        log.info("SearchPurchase started (id={}, countryCurrency={})", id, countryCurrency);

        Transaction transaction = transactionGateway.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        ExchangeRate rate = findExchangeRate(countryCurrency, transaction);

        log.info("SearchPurchase finished (id={}, countryCurrency={})",
                id, countryCurrency);

        return prepareConvertedTransaction(transaction, rate);
    }

    private ExchangeRate findExchangeRate(String countryCurrency, Transaction transaction) {
        LocalDate purchaseDate = transaction.transactionDate();
        LocalDate minDate = purchaseDate.minus(LOOKBACK);

        ExchangeRate rate = treasuryGateway.findLatestRate(countryCurrency, purchaseDate)
                .orElseThrow(() -> new ExchangeRateNotFoundException(countryCurrency, purchaseDate));

        if (rate.effectiveDate().isBefore(minDate)) {
            throw new ExchangeRateNotFoundException(countryCurrency, purchaseDate);
        }

        return rate;
    }

    private ConvertedTransaction prepareConvertedTransaction(Transaction transaction, ExchangeRate rate) {

        return new ConvertedTransaction(
                transaction.id(),
                transaction.description(),
                transaction.transactionDate(),
                transaction.amount(),
                rate.exchangeRate(),
                convertAmount(transaction.amount(), rate.exchangeRate())
        );
    }

    private BigDecimal convertAmount(BigDecimal amount, BigDecimal exchangeRate) {
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (exchangeRate == null) throw new IllegalArgumentException("exchangeRate must not be null");

        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

}
