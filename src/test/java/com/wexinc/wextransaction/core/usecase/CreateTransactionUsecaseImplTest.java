package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.gateway.TransactionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTransactionUsecaseImplTest {

    private TransactionGateway transactionGateway;
    private CreateTransactionUsecaseImpl usecase;

    @BeforeEach
    void setup() {
        transactionGateway = mock(TransactionGateway.class);
        usecase = new CreateTransactionUsecaseImpl(transactionGateway);
    }

    @Test
    void execute_shouldRoundAmountToNearestCent_halfUp_andReturnId() {
        // given
        Transaction input = new Transaction(
                null,
                "Coffee",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.555") // -> 10.56 (HALF_UP)
        );

        // gateway returns saved transaction with id
        when(transactionGateway.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction normalized = invocation.getArgument(0, Transaction.class);
                    return new Transaction(
                            42L,
                            normalized.description(),
                            normalized.transactionDate(),
                            normalized.amount()
                    );
                });

        // when
        Long id = usecase.execute(input);

        // then
        assertEquals(42L, id);

        verify(transactionGateway, times(1)).save(argThat(tx ->
                tx.id() == null &&
                        tx.description().equals("Coffee") &&
                        tx.transactionDate().equals(LocalDate.of(2025, 12, 1)) &&
                        tx.amount().compareTo(new BigDecimal("10.56")) == 0
        ));
        verifyNoMoreInteractions(transactionGateway);
    }

    @Test
    void execute_shouldRoundHalfDownCaseCorrectly() {
        // given: 10.554 -> 10.55
        Transaction input = new Transaction(
                null,
                "Tea",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.554")
        );

        when(transactionGateway.save(any(Transaction.class)))
                .thenReturn(new Transaction(
                        7L,
                        input.description(),
                        input.transactionDate(),
                        new BigDecimal("10.55")
                ));

        // when
        Long id = usecase.execute(input);

        // then
        assertEquals(7L, id);

        verify(transactionGateway).save(argThat(tx ->
                tx.amount().compareTo(new BigDecimal("10.55")) == 0
        ));
        verifyNoMoreInteractions(transactionGateway);
    }

    @Test
    void execute_whenTransactionIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> usecase.execute(null)
        );
        assertEquals("Transaction must not be null", ex.getMessage());

        verifyNoInteractions(transactionGateway);
    }

    @Test
    void execute_whenAmountIsNull_shouldThrowIllegalArgumentException() {
        Transaction input = new Transaction(
                null,
                "Something",
                LocalDate.of(2025, 12, 1),
                null
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> usecase.execute(input)
        );
        assertEquals("Transaction amount must not be null", ex.getMessage());

        verifyNoInteractions(transactionGateway);
    }
}
