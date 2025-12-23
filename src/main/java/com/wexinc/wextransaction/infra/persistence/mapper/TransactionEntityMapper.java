package com.wexinc.wextransaction.infra.persistence.mapper;

import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.infra.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionEntityMapper {

    Transaction toDomain(TransactionEntity entity);
    TransactionEntity toEntity(Transaction domain);
}
