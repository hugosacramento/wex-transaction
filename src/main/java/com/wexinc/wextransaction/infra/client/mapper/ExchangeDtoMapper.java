package com.wexinc.wextransaction.infra.client.mapper;

import com.wexinc.wextransaction.core.domain.model.ExchangeRate;
import com.wexinc.wextransaction.infra.client.dto.TreasuryRateData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExchangeDtoMapper {

    ExchangeRate toDomain(TreasuryRateData entity);
}
