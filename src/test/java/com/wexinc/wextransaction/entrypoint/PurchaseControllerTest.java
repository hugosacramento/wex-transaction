package com.wexinc.wextransaction.entrypoint;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;
import com.wexinc.wextransaction.core.domain.model.Transaction;
import com.wexinc.wextransaction.core.exception.TransactionNotFoundException;
import com.wexinc.wextransaction.core.usecase.CreateTransactionUsecase;
import com.wexinc.wextransaction.core.usecase.SearchPurchaseUsecase;
import com.wexinc.wextransaction.entrypoint.dto.CreateTransactionRequest;
import com.wexinc.wextransaction.entrypoint.dto.ConvertedTransactionResponse;
import com.wexinc.wextransaction.entrypoint.dto.mapper.TransactionDtoMapper;
import com.wexinc.wextransaction.entrypoint.exception.GlobalHandlerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@Import(GlobalHandlerException.class)
class PurchaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CreateTransactionUsecase createTransactionUsecase;

    @MockitoBean
    SearchPurchaseUsecase searchPurchaseUsecase;

    @MockitoBean
    TransactionDtoMapper mapper;

    @Test
    void createTransaction_shouldReturn201AndLocation() throws Exception {
        // Ajuste se seu CreateTransactionRequest não for record
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50") // amount
        );

//        Transaction domain = new Transaction();
//        when(mapper.toDomain(any(CreateTransactionRequest.class))).thenReturn(domain);
        when(createTransactionUsecase.execute(any())).thenReturn(42L);

        mockMvc.perform(post("/purchase/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/purchase/transaction/42"))
                .andExpect(content().string(""));

        verify(mapper).toDomain(any(CreateTransactionRequest.class));
        verify(createTransactionUsecase).execute(any());
        verifyNoMoreInteractions(createTransactionUsecase, searchPurchaseUsecase, mapper);
    }

    @Test
    void createTransaction_whenInvalidBody_shouldReturn400WithApiError() throws Exception {
        // description blank, amount null -> deve disparar @Valid
        String invalidJson = """
                {
                  "description": "",
                  "transactionDate": "2025-12-01",
                  "amount": null
                }
                """;

        mockMvc.perform(post("/purchase/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.path").value("/purchase/transaction"))
                .andExpect(jsonPath("$.details.description").exists());

        verifyNoInteractions(createTransactionUsecase, searchPurchaseUsecase, mapper);
    }

    @Test
    void retrievePurchaseTransaction_shouldReturn200AndJson() throws Exception {
        Long id = 99L;
        String countryCurrency = "Canada-Dollar";

        ConvertedTransaction converted = new ConvertedTransaction(
                id,
                "Office supplies",
                LocalDate.of(2025, 12, 1),
                new BigDecimal("10.50"),
                new BigDecimal("1.549"),
                new BigDecimal("6.78")
        );

        ConvertedTransactionResponse response = new ConvertedTransactionResponse(
                converted.id(),
                converted.description(),
                converted.transactionDate(),
                converted.amount(),
                converted.exchangeRate(),
                converted.convertedAmount()
        );

        when(searchPurchaseUsecase.execute(id, countryCurrency)).thenReturn(converted);
        when(mapper.toConvertedResponse(converted)).thenReturn(response);

        mockMvc.perform(get("/purchase/transaction/{id}/convert", id)
                        .param("country_currency", countryCurrency)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.description").value("Office supplies"))
                .andExpect(jsonPath("$.transactionDate").value("2025-12-01"))
                .andExpect(jsonPath("$.amount").value(10.50))
                .andExpect(jsonPath("$.exchangeRate").value(1.549))
                .andExpect(jsonPath("$.convertedAmount").value(6.78));

        verify(searchPurchaseUsecase).execute(id, countryCurrency);
        verify(mapper).toConvertedResponse(converted);
        verifyNoMoreInteractions(createTransactionUsecase, searchPurchaseUsecase, mapper);
    }

    @Test
    void retrievePurchaseTransaction_whenMissingCountryCurrency_shouldReturn400() throws Exception {
        mockMvc.perform(get("/purchase/transaction/{id}/convert", 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createTransactionUsecase, searchPurchaseUsecase, mapper);
    }

    @Test
    void retrievePurchaseTransaction_whenTransactionNotFound_shouldReturn404WithApiError() throws Exception {
        Long id = 123L;
        String countryCurrency = "Canada-Dollar";

        when(searchPurchaseUsecase.execute(id, countryCurrency))
                .thenThrow(new TransactionNotFoundException(id));

        mockMvc.perform(get("/purchase/transaction/{id}/convert", id)
                        .param("country_currency", countryCurrency)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Transaction not found"))
                .andExpect(jsonPath("$.path").value("/purchase/transaction/123/convert"));

        verify(searchPurchaseUsecase).execute(id, countryCurrency);
        verifyNoMoreInteractions(createTransactionUsecase, searchPurchaseUsecase, mapper);
    }
}