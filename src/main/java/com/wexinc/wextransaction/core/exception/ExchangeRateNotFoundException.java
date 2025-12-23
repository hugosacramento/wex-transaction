package com.wexinc.wextransaction.core.exception;

import java.time.LocalDate;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(String desc, LocalDate date) {
        super("No exchange rate found for " + desc + " within 6 months on/before " + date);
    }
}