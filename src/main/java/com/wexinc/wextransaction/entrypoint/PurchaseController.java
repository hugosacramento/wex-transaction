package com.wexinc.wextransaction.entrypoint;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;
import com.wexinc.wextransaction.core.usecase.CreateTransactionUsecase;
import com.wexinc.wextransaction.core.usecase.SearchPurchaseUsecase;
import com.wexinc.wextransaction.entrypoint.dto.ConvertedTransactionResponse;
import com.wexinc.wextransaction.entrypoint.dto.CreateTransactionRequest;
import com.wexinc.wextransaction.entrypoint.dto.mapper.TransactionDtoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@Validated
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final CreateTransactionUsecase createTransactionUsecase;
    private final SearchPurchaseUsecase searchPurchaseUsecase;

    private final TransactionDtoMapper mapper;

    @PostMapping("/transaction")
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid CreateTransactionRequest createTransactionRequest) {
        log.info("Creating transaction description: {}", createTransactionRequest.description());
        Long id = createTransactionUsecase.execute(mapper.toDomain(createTransactionRequest));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        log.info("Transaction created id: {}", id);
        return ResponseEntity.created(location).build();
    }

}
