package com.wexinc.wextransaction.core.gateway;

import com.wexinc.wextransaction.core.domain.model.Transaction;

import java.util.Optional;

public interface TransactionGateway {

    Optional<Transaction> findById(Long id);
    Transaction save(Transaction transaction);
}
