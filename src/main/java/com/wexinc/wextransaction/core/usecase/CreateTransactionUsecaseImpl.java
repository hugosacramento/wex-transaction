package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.gateway.TransactionGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Slf4j
@Service
public class CreateTransactionUsecaseImpl implements CreateTransactionUsecase {

    private final TransactionGateway transactionGateway;

    public CreateTransactionUsecaseImpl(TransactionGateway transactionGateway) {
        this.transactionGateway = transactionGateway;
    }

    @Override
    public Long execute(Transaction transaction) {
        log.info("Execute CreateTransactionUseCase start");
        Transaction normalized = setAmountRoundedToNearestCent(transaction);

        Long id = transactionGateway.save(normalized).id();

        log.info("Transaction created (id={})", id);
        return id;
    }

    private Transaction setAmountRoundedToNearestCent(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }
        if (transaction.amount() == null) {
            throw new IllegalArgumentException("Transaction amount must not be null");
        }

        return transaction.withAmount(transaction.amount().setScale(2, RoundingMode.HALF_UP));
    }
}
