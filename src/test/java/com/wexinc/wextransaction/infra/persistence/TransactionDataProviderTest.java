package com.wexinc.wextransaction.infra.persistence;

import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.infra.persistence.entity.TransactionEntity;
import com.wexinc.wextransaction.infra.persistence.mapper.TransactionEntityMapper;
import com.wexinc.wextransaction.infra.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionDataProviderTest {

    private TransactionRepository transactionRepository;
    private TransactionDataProvider dataProvider;
    private TransactionEntityMapper mapper;

    @BeforeEach
    void setup() {
        transactionRepository = mock(TransactionRepository.class);
        mapper = Mappers.getMapper(TransactionEntityMapper.class);
        dataProvider = new TransactionDataProvider(transactionRepository, mapper);
    }

    @Test
    void save_shouldMapAndPersistAndReturnDomain() {
        // given
        Transaction input = new Transaction(
                null,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50")
        );

        TransactionEntity savedEntity = new TransactionEntity(
                42L,
                input.description(),
                input.transactionDate(),
                input.amount()
        );

        Transaction mappedBack = new Transaction(
                42L,
                input.description(),
                input.transactionDate(),
                input.amount()
        );

        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(savedEntity);

        // when
        Transaction out = dataProvider.save(input);

        // then
        assertEquals(mappedBack, out);

        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void save_whenRepositoryThrows_shouldPropagateException() {
        // given
        Transaction input = new Transaction(
                null,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50")
        );

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenThrow(new DataAccessResourceFailureException("DB connection error"));

        // when / then
        assertThrows(DataAccessResourceFailureException.class, () -> dataProvider.save(input));

        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void findById_whenFound_shouldReturnMappedDomain() {
        Long id = 10L;

        TransactionEntity entity = new TransactionEntity(
                id,
                "X",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("1.00")
        );
        Transaction mapped = new Transaction(
                entity.getId(),
                entity.getDescription(),
                entity.getTransactionDate(),
                entity.getAmount());

        when(transactionRepository.findById(id)).thenReturn(java.util.Optional.of(entity));

        var out = dataProvider.findById(id);

        assertTrue(out.isPresent());
        assertEquals(mapped, out.get());

        verify(transactionRepository).findById(id);
    }

    @Test
    void findById_whenNotFound_shouldReturnEmpty() {
        Long id = 999L;

        when(transactionRepository.findById(id)).thenReturn(java.util.Optional.empty());

        var out = dataProvider.findById(id);

        assertTrue(out.isEmpty());

        verify(transactionRepository).findById(id);
    }

}
