package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;
import com.wexinc.wextransaction.core.domain.model.ExchangeRate;
import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.exception.ExchangeRateNotFoundException;
import com.wexinc.wextransaction.core.exception.TransactionNotFoundException;
import com.wexinc.wextransaction.core.gateway.TransactionGateway;
import com.wexinc.wextransaction.core.gateway.TreasuryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchPurchaseUsecaseImplTest {

    private TransactionGateway transactionGateway;
    private TreasuryGateway treasuryGateway;
    private SearchPurchaseUsecaseImpl usecase;

    @BeforeEach
    void setup() {
        transactionGateway = mock(TransactionGateway.class);
        treasuryGateway = mock(TreasuryGateway.class);
        usecase = new SearchPurchaseUsecaseImpl(transactionGateway, treasuryGateway);
    }

    @Test
    void execute_shouldReturnConvertedTransaction_whenFoundAndRateWithinLookback() {
        // given
        Long id = 10L;
        String currency = "Canada-Dollar";

        Transaction tx = new Transaction(
                id,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50")
        );

        // effective date within 6 months of 2025-12-01 (min date 2025-06-01)
        ExchangeRate rate = new ExchangeRate(
                currency,
                new BigDecimal("1.549"),
                LocalDate.of(2025, 9, 30)
        );

        when(transactionGateway.findById(id)).thenReturn(Optional.of(tx));
        when(treasuryGateway.findLatestRate(currency, tx.transactionDate())).thenReturn(Optional.of(rate));

        // when
        ConvertedTransaction out = usecase.execute(id, currency);

        // then
        assertNotNull(out);
        assertEquals(id, out.id());
        assertEquals("Office supplies", out.description());
        assertEquals(LocalDate.of(2025, 12, 1), out.transactionDate());
        assertEquals(new BigDecimal("10.50"), out.amount());
        assertEquals(new BigDecimal("1.549"), out.exchangeRate());

        // 10.50 / 1.549 = 6.778... => 6.78 (HALF_UP)
        assertEquals(new BigDecimal("16.26"), out.convertedAmount());

        verify(transactionGateway).findById(id);
        verify(treasuryGateway).findLatestRate(currency, tx.transactionDate());
        verifyNoMoreInteractions(transactionGateway, treasuryGateway);
    }

    @Test
    void execute_whenTransactionNotFound_shouldThrowTransactionNotFoundException() {
        // given
        Long id = 999L;
        when(transactionGateway.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThrows(TransactionNotFoundException.class, () -> usecase.execute(id, "Canada-Dollar"));

        verify(transactionGateway).findById(id);
        verifyNoMoreInteractions(transactionGateway, treasuryGateway);
    }

    @Test
    void execute_whenRateNotFound_shouldThrowExchangeRateNotFoundException() {
        // given
        Long id = 10L;
        String currency = "Canada-Dollar";

        Transaction tx = new Transaction(
                id,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50")
        );

        when(transactionGateway.findById(id)).thenReturn(Optional.of(tx));
        when(treasuryGateway.findLatestRate(currency, tx.transactionDate())).thenReturn(Optional.empty());

        // when / then
        assertThrows(ExchangeRateNotFoundException.class, () -> usecase.execute(id, currency));

        verify(transactionGateway).findById(id);
        verify(treasuryGateway).findLatestRate(currency, tx.transactionDate());
        verifyNoMoreInteractions(transactionGateway, treasuryGateway);
    }

    @Test
    void execute_whenRateEffectiveDateOlderThanLookback_shouldThrowExchangeRateNotFoundException() {
        // given
        Long id = 10L;
        String currency = "Canada-Dollar";

        Transaction tx = new Transaction(
                id,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50")
        );

        // minDate = 2025-06-01; this is older => should throw
        ExchangeRate oldRate = new ExchangeRate(
                currency,
                new BigDecimal("1.500"),
                LocalDate.of(2025, 5, 31)
        );

        when(transactionGateway.findById(id)).thenReturn(Optional.of(tx));
        when(treasuryGateway.findLatestRate(currency, tx.transactionDate())).thenReturn(Optional.of(oldRate));

        // when / then
        assertThrows(ExchangeRateNotFoundException.class, () -> usecase.execute(id, currency));

        verify(transactionGateway).findById(id);
        verify(treasuryGateway).findLatestRate(currency, tx.transactionDate());
        verifyNoMoreInteractions(transactionGateway, treasuryGateway);
    }

    @Test
    void convertAmount_whenAmountNull_shouldThrowIllegalArgumentException() throws Exception {
        Method m = SearchPurchaseUsecaseImpl.class.getDeclaredMethod("convertAmount", BigDecimal.class, BigDecimal.class);
        m.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () ->
                m.invoke(usecase, null, new BigDecimal("1.00"))
        );

        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("amount must not be null", ex.getCause().getMessage());
    }

    @Test
    void convertAmount_whenExchangeRateNull_shouldThrowIllegalArgumentException() throws Exception {
        Method m = SearchPurchaseUsecaseImpl.class.getDeclaredMethod("convertAmount", BigDecimal.class, BigDecimal.class);
        m.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () ->
                m.invoke(usecase, new BigDecimal("10.00"), null)
        );

        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("exchangeRate must not be null", ex.getCause().getMessage());
    }
}
