package com.upstox.production.nifty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderType {

    BUY_ENTRY("BUY_ENTRY"),
    BUY_EXIT("BUY_EXIT"),
    SELL_ENTRY("SELL_ENTRY"),
    SELL_EXIT("SELL_EXIT");

    private String orderType;
}
