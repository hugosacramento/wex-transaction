package com.wexinc.wextransaction.infra.client.feign;

import com.wexinc.wextransaction.infra.client.dto.TreasuryRatesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "treasuryFiscalDataClient",
        url = "${app.treasury.base-url}"
)
public interface TreasuryFeign {

    @GetMapping("/v1/accounting/od/rates_of_exchange")
    TreasuryRatesResponse getRatesOfExchange(
            @RequestParam("filter") String filter,
            @RequestParam(value = "fields", required = false) String fields,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page[size]", required = false) Integer pageSize
    );
}