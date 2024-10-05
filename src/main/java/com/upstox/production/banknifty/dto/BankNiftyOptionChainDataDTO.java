package com.upstox.production.banknifty.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BankNiftyOptionChainDataDTO {
    private String expiry;
    private double strike_price;
    private String underlying_key;
    private double underlying_spot_price;
    private BankNiftyOptionDTO call_options;
    private BankNiftyOptionDTO put_options;
    private double pcr;
}