package com.tmoney;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
class Account {
    private String number;
    private String name;
    private BigDecimal balance;
}
