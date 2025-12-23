package com.wexinc.wextransaction.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreasuryRatesResponse {
    private List<TreasuryRateData> data;
}