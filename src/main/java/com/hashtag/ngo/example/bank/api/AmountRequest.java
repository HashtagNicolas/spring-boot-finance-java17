package com.hashtag.ngo.example.bank.api;

import java.math.BigDecimal;

/** Corps de requête pour un dépôt ou un retrait. */
public record AmountRequest(BigDecimal amount) {
}
