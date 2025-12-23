package com.wexinc.wextransaction.entrypoint.dto.mapper;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;
import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.entrypoint.dto.ConvertedTransactionResponse;
import com.wexinc.wextransaction.entrypoint.dto.CreateTransactionRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionDtoMapper {

    Transaction toDomain(CreateTransactionRequest dto);
}
