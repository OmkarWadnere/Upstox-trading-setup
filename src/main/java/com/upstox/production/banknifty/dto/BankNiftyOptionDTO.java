package com.upstox.production.banknifty.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BankNiftyOptionDTO {
    private String instrument_key;
    private BankNiftyOptionMarketDataDTO market_data;
    private BankNiftyOptionGreeksDTO option_greeks;
}