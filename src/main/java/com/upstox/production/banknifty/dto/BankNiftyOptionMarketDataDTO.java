package com.upstox.production.banknifty.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BankNiftyOptionMarketDataDTO {
    private double ltp;
    private int volume;
    private int oi;
    private double close_price;
    private double bid_price;
    private int bid_qty;
    private double ask_price;
    private int ask_qty;
    private int prev_oi;
}