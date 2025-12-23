package com.wexinc.wextransaction.infra.persistence;

import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.gateway.TransactionGateway;
import com.wexinc.wextransaction.infra.persistence.entity.TransactionEntity;
import com.wexinc.wextransaction.infra.persistence.mapper.TransactionEntityMapper;
import com.wexinc.wextransaction.infra.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionDataProvider implements TransactionGateway {

    private final TransactionRepository transactionRepository;
    private final TransactionEntityMapper mapper;

    @Override
    public Optional<Transaction> findById(Long id) {
        log.info("Finding transaction by id={}", id);
        return transactionRepository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Transaction save(Transaction transaction) {
        log.info("Saving transaction {}", transaction);
        TransactionEntity entity = transactionRepository.save(mapper.toEntity(transaction));

        log.info("Transaction saved {}", entity);
        return mapper.toDomain(entity);
    }
}
