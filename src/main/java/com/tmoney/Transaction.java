package com.tmoney;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
class Transaction {
    private Long id;
    private String debit;
    private String credit;
    private BigDecimal amount;
}
