package com.upstox.production.nifty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class NiftyOptionDTO {
    private String instrument_key;
    private NiftyOptionMarketDataDTO market_data;
    private NiftyOptionGreeksDTO option_greeks;
}