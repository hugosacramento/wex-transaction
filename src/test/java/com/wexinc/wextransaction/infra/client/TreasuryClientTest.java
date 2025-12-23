package com.wexinc.wextransaction.infra.client;

import com.wexinc.wextransaction.core.domain.model.ExchangeRate;
import com.wexinc.wextransaction.infra.client.dto.TreasuryRateData;
import com.wexinc.wextransaction.infra.client.dto.TreasuryRatesResponse;
import com.wexinc.wextransaction.infra.client.feign.TreasuryFeign;
import com.wexinc.wextransaction.infra.client.mapper.ExchangeDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TreasuryClientTest {

    private TreasuryFeign treasuryFeign;
    private ExchangeDtoMapper mapper;
    private TreasuryClient client;

    @BeforeEach
    void setup() {
        treasuryFeign = mock(TreasuryFeign.class);
        mapper = Mappers.getMapper(ExchangeDtoMapper.class);
        client = new TreasuryClient(treasuryFeign, mapper);
    }

    @Test
    void findLatestRate_shouldBuildQueryAndCallFeignWithExpectedParams() {
        // given
        String countryCurrencyDesc = "Canada-Dollar";
        LocalDate date = LocalDate.of(2001, 10, 30);

        TreasuryRateData row = new TreasuryRateData();
        row.setCountryCurrencyDesc("Canada-Dollar");
        row.setExchangeRate(new BigDecimal("1.549"));
        row.setEffectiveDate(LocalDate.of(2001, 9, 30));

        TreasuryRatesResponse response = new TreasuryRatesResponse();
        response.setData(List.of(row));

        when(treasuryFeign.getRatesOfExchange(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(response);

        // when
        Optional<ExchangeRate> out = client.findLatestRate(countryCurrencyDesc, date);

        // then
        assertTrue(out.isPresent());
        assertEquals("Canada-Dollar", out.get().countryCurrencyDesc());
        assertEquals(new BigDecimal("1.549"), out.get().exchangeRate());
        assertEquals(LocalDate.of(2001, 9, 30), out.get().effectiveDate());

        ArgumentCaptor<String> filterCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldsCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sortCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageSizeCap = ArgumentCaptor.forClass(Integer.class);

        verify(treasuryFeign).getRatesOfExchange(
                filterCap.capture(),
                fieldsCap.capture(),
                sortCap.capture(),
                pageSizeCap.capture()
        );

        assertEquals(
                "country_currency_desc:eq:Canada-Dollar,effective_date:lte:2001-10-30",
                filterCap.getValue()
        );
        assertEquals("effective_date,exchange_rate,country_currency_desc", fieldsCap.getValue());
        assertEquals("-effective_date", sortCap.getValue());
        assertEquals(1, pageSizeCap.getValue());

        verifyNoMoreInteractions(treasuryFeign);
    }

    @Test
    void findLatestRate_whenFeignReturnsNull_shouldReturnEmpty() {
        // given
        when(treasuryFeign.getRatesOfExchange(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(null);

        // when
        Optional<ExchangeRate> out = client.findLatestRate("Canada-Dollar", LocalDate.of(2001, 10, 30));

        // then
        assertTrue(out.isEmpty());
        verify(treasuryFeign).getRatesOfExchange(anyString(), anyString(), anyString(), anyInt());
        verifyNoMoreInteractions(treasuryFeign);
    }

    @Test
    void findLatestRate_whenResponseDataNull_shouldReturnEmpty() {
        // given
        TreasuryRatesResponse response = new TreasuryRatesResponse();
        response.setData(null);

        when(treasuryFeign.getRatesOfExchange(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(response);

        // when
        Optional<ExchangeRate> out = client.findLatestRate("Canada-Dollar", LocalDate.of(2001, 10, 30));

        // then
        assertTrue(out.isEmpty());
        verify(treasuryFeign).getRatesOfExchange(anyString(), anyString(), anyString(), anyInt());
        verifyNoMoreInteractions(treasuryFeign);
    }

    @Test
    void findLatestRate_whenResponseDataEmpty_shouldReturnEmpty() {
        // given
        TreasuryRatesResponse response = new TreasuryRatesResponse();
        response.setData(List.of());

        when(treasuryFeign.getRatesOfExchange(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(response);

        // when
        Optional<ExchangeRate> out = client.findLatestRate("Canada-Dollar", LocalDate.of(2001, 10, 30));

        // then
        assertTrue(out.isEmpty());
        verify(treasuryFeign).getRatesOfExchange(anyString(), anyString(), anyString(), anyInt());
        verifyNoMoreInteractions(treasuryFeign);
    }

    @Test
    void findLatestRate_whenMultipleRows_shouldUseFirstRowOnly() {
        // given
        String countryCurrencyDesc = "Canada-Dollar";
        LocalDate date = LocalDate.of(2001, 10, 30);

        TreasuryRateData first = new TreasuryRateData();
        first.setCountryCurrencyDesc("Canada-Dollar");
        first.setExchangeRate(new BigDecimal("1.549"));
        first.setEffectiveDate(LocalDate.of(2001, 9, 30));

        TreasuryRateData second = new TreasuryRateData();
        second.setCountryCurrencyDesc("Canada-Dollar");
        second.setExchangeRate(new BigDecimal("1.541"));
        second.setEffectiveDate(LocalDate.of(2001, 6, 30));

        TreasuryRatesResponse response = new TreasuryRatesResponse();
        response.setData(List.of(first, second));

        when(treasuryFeign.getRatesOfExchange(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(response);

        // when
        Optional<ExchangeRate> out = client.findLatestRate(countryCurrencyDesc, date);

        // then
        assertTrue(out.isPresent());
        assertEquals(new BigDecimal("1.549"), out.get().exchangeRate());
        assertEquals(LocalDate.of(2001, 9, 30), out.get().effectiveDate());

        verify(treasuryFeign).getRatesOfExchange(anyString(), anyString(), anyString(), anyInt());
        verifyNoMoreInteractions(treasuryFeign);
    }
}
