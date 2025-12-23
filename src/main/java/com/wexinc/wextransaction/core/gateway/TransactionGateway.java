package com.wexinc.wextransaction.core.gateway;

import com.wexinc.wextransaction.core.domain.model.Transaction;

public interface TransactionGateway {

    Transaction save(Transaction transaction);
}
