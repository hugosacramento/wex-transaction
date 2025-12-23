package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.Transaction;

public interface CreateTransactionUsecase {
    Long execute(Transaction transaction);
}
