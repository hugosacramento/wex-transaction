package com.wexinc.wextransaction.core.usecase;

import com.wexinc.wextransaction.core.domain.model.ConvertedTransaction;

public interface SearchPurchaseUsecase {
    ConvertedTransaction execute(Long id, String countryCurrency);
}
